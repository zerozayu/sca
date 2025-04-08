package com.zhangyu.sca.module.pay.service.impl;

import com.zhangyu.sca.module.pay.service.SignInService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
@Slf4j
public class SignInServiceImpl implements SignInService {

    private final RedisTemplate<String, String> redisTemplate;

    private static final String SIGN_IN_KEY = "sign_in:%s:%s";
    private static final String RESIGN_IN_KEY = "resign_in:%s:%s";
    private static final String SIGN_IN_TOTAL_KEY = "sign_in_total:%s:%s";

    public SignInServiceImpl(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
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

        // 4. 未签到，更新签到记录 signBitmap，
        redisTemplate.opsForValue().setBit(signInKey, dayOfMonth, true);

        // 5. 计算签到天数
        byte[] signInBitmap = redisTemplate.execute((RedisCallback<byte[]>)
                conn -> conn.stringCommands().get(signInKey.getBytes()));

        // 补签和签到 bitmap 取并集
        redisTemplate.execute((RedisCallback<Long>) conn -> conn
                .stringCommands()
                .bitOp(RedisStringCommands.BitOperation.OR, signInTotalKey.getBytes(), resignInKey.getBytes(), signInKey.getBytes())
        );

        // 计算累计签到天数
        Long monthDays = redisTemplate.execute((RedisCallback<Long>) conn ->
                conn.stringCommands().bitCount(signInTotalKey.getBytes()));


        // 6. 计算连续正常签到天数和连续签到天数
        int consecutiveDays = calculateConsecutiveDays(signInBitmap, dayOfMonth);

        // 7. 更新入库
        log.info("userId: {}, yyyyMM: {}, signInKey: {}, signInTotalKey: {}, dayOfMonth: {}, consecutiveDays: {}, " +
                "monthDays: {}", userId, yyyyMM, signInKey, signInTotalKey, dayOfMonth, consecutiveDays, monthDays);


        return true;
    }


    // 计算连续天数
    private int calculateConsecutiveDays(byte[] bitmap, int length) {
        String binaryString = toBitString(bitmap);// 0000001011110000
        for (int i = length; i > 0; i--) {
            if (binaryString.charAt(i - 1) == '0') {
                return length - i + 1;
            }
        }
        return length;
    }

    public String toBitString(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%8s", Integer.toBinaryString(b & 0xFF)).replace(" ", "0"));
        }
        return sb.toString();
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
