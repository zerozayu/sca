package com.zhangyu.sca.module.pay.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhangyu.sca.module.pay.domain.SignInInfo;
import com.zhangyu.sca.module.pay.service.SignInInfoService;
import com.zhangyu.sca.module.pay.mapper.SignInInfoMapper;
import org.springframework.stereotype.Service;

/**
* @author zhangyu
* @description 针对表【pay_sign_in_info(签到信息表)】的数据库操作Service实现
* @createDate 2025-04-03 17:20:37
*/
@Service
public class SignInInfoServiceImpl extends ServiceImpl<SignInInfoMapper, SignInInfo>
    implements SignInInfoService{

}




