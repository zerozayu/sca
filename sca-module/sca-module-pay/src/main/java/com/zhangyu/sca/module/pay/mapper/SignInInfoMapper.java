package com.zhangyu.sca.module.pay.mapper;

import com.zhangyu.sca.module.pay.domain.SignInInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;

/**
 * @author zhangyu
 * @description 针对表【pay_sign_in_info(签到信息表)】的数据库操作Mapper
 * @createDate 2025-04-03 17:20:37
 * @Entity com.zhangyu.sca.module.pay.domain.SignInInfo
 */
public interface SignInInfoMapper extends BaseMapper<SignInInfo> {

    @Select("select * from pay_sign_in_info where sign_in_key = #{signInKey}")
    SignInInfo selectBySignInKey(String signInKey);
}




