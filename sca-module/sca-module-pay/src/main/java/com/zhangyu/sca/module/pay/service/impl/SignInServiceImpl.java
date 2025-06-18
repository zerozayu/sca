package com.zhangyu.sca.module.pay.service.impl;

import com.zhangyu.sca.module.pay.domain.SignInInfo;
import com.zhangyu.sca.module.pay.mapper.SignInInfoMapper;
import com.zhangyu.sca.module.pay.service.SignInInfoService;
import com.zhangyu.sca.module.pay.service.SignInService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
@Slf4j
public class SignInServiceImpl implements SignInService {


    private static final String SIGN_IN_KEY = "sign_in:%s:%s";
    private static final String RESIGN_IN_KEY = "resign_in:%s:%s";
    private static final String SIGN_IN_TOTAL_KEY = "sign_in_total:%s:%s";
    private final SignInInfoService signInInfoService;
    private final SignInInfoMapper signInInfoMapper;
    private final RedisTemplate<String, String> redisTemplate;
    private final SignInService signInService;

    public SignInServiceImpl(RedisTemplate<String, String> redisTemplate,
                             SignInInfoService signInInfoService,
                             SignInInfoMapper signInInfoMapper,
                             @Lazy SignInService signInService) {
        this.redisTemplate = redisTemplate;
        this.signInInfoService = signInInfoService;
        this.signInInfoMapper = signInInfoMapper;
        this.signInService = signInService;
    }

    @Override
    public Boolean signIn(Long userId) {
        // 1. 初始化
        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMM");
        String yyyyMM = today.format(formatter);
        String signInKey = String.format(SIGN_IN_KEY, yyyyMM, userId);
        String resignInKey = String.format(RESIGN_IN_KEY, yyyyMM, userId);
        String signInTotalKey = String.format(SIGN_IN_TOTAL_KEY, yyyyMM, userId);
        int dayOfMonth = today.getDayOfMonth();

        // SignInService signInService = applicationContext.getBean("signInService", SignInService.class);
        // 判断是否签到
        if (signInService.judgeIsSign(signInKey, dayOfMonth)) {
            return true;
        }


        // 4. 未签到，更新签到记录 signBitmap，
        redisTemplate.opsForValue().setBit(signInKey, dayOfMonth, true);

        // 5. 签到日历
        byte[] signInBitmap = redisTemplate.execute((RedisCallback<byte[]>)
                conn -> conn.stringCommands().get(signInKey.getBytes()));
        byte[] resignInBitmap = redisTemplate.execute((RedisCallback<byte[]>)
                conn -> conn.stringCommands().get(resignInKey.getBytes()));

        // 补签和签到 bitmap 取并集
        redisTemplate.execute((RedisCallback<Long>) conn -> conn
                .stringCommands()
                .bitOp(RedisStringCommands.BitOperation.OR, signInTotalKey.getBytes(), resignInKey.getBytes(), signInKey.getBytes())
        );

        // 计算累计签到天数
        Long monthDays = redisTemplate.execute((RedisCallback<Long>) conn ->
                conn.stringCommands().bitCount(signInTotalKey.getBytes()));


        // 6. 计算连续正常签到天数
        int consecutiveDays = calculateConsecutiveDays(signInBitmap, dayOfMonth);

        // 计算连续签到奖励
        BigDecimal consecutiveRewards = calculateRewards(consecutiveDays);

        // 计算累计签到奖励
        BigDecimal totalRewards = calculateTotalRewards(monthDays);


        log.info("""
                        userId: {}, yyyyMM: {}, signInKey: {}, signInTotalKey: {}, dayOfMonth: {}, consecutiveDays: {}, monthDays: {}, consecutiveRewards: {}
                        """,
                userId, yyyyMM, signInKey, signInTotalKey, dayOfMonth, consecutiveDays, monthDays, consecutiveRewards);

        // 签到信息
        SignInInfo signInInfo = SignInInfo.builder()
                .userId(userId)
                .signInMonth(yyyyMM)
                .signInCalendar(signInBitmap)
                .resignInCalendar(resignInBitmap)
                .signInDays(monthDays.intValue())
                .continuousSignInDays(consecutiveDays)
                .build();

        signInInfoService.saveOrUpdate(signInInfo);

        return true;
    }

    @Override
    public boolean judgeIsSign(String signInKey, int dayOfMonth) {
        // 查询数据库，查看当天是否签到
        SignInInfo signInInfo = signInInfoMapper.selectBySignInKey(signInKey);
        return signInInfo == null || signInInfo.getSignInCalendar()[dayOfMonth] != 1;
    }

    /**
     * 计算奖励
     *
     * @param monthDays
     * @return
     */
    private BigDecimal calculateTotalRewards(Long monthDays) {
        return switch (monthDays.intValue()) {
            case 3, 7, 15, 30 -> BigDecimal.valueOf(monthDays * 10);
            default -> BigDecimal.ZERO;
        };
    }

    private BigDecimal calculateRewards(long consecutiveDays) {
        return BigDecimal.ZERO.add(
                BigDecimal.valueOf(20)
                        .multiply(BigDecimal.valueOf(consecutiveDays >= 7 ? 7 : consecutiveDays))
        );
    }


    /**
     * 计算连续天数
     */
    private int calculateConsecutiveDays(byte[] bitmap, int dayOfMonth) {
        String binaryString = toBitString(bitmap, dayOfMonth);// 0000001011110000
        // 从最后一天开始向前遍历
        for (int i = dayOfMonth; i > 0; i--) {
            if (binaryString.charAt(i - 1) == '0') {
                return dayOfMonth - i + 1;
            }
        }
        return dayOfMonth;
    }

    /**
     * 将 byte[] 转换为二进制字符串
     *
     * @param bytes
     * @return
     */
    public String toBitString(byte[] bytes, int length) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%8s", Integer.toBinaryString(b & 0xFF)).replace(" ", "0"));
        }
        return sb.substring(0, length);
    }

    // 对两个 byte[] 做按位或（OR）
    public static byte[] bitwiseOr(byte[] a, byte[] b) {
        // 计算两个数组中较长的长度
        int maxLength = Math.max(a.length, b.length);
        // 创建一个新数组，长度为较长的数组长度
        byte[] result = new byte[maxLength];

        // 遍历较长的数组长度次
        for (int i = 0; i < maxLength; i++) {
            // 如果当前索引小于数组a的长度，则取数组a中的值，否则取0
            byte byteA = i < a.length ? a[i] : 0;
            // 如果当前索引小于数组b的长度，则取数组b中的值，否则取0
            byte byteB = i < b.length ? b[i] : 0;
            // 将两个值进行按位或操作，并将结果存入结果数组中
            result[i] = (byte) (byteA | byteB);
        }

        // 返回结果数组
        return result;
    }

}
