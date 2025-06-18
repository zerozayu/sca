package com.zhangyu.sca.module.pay.service;

import org.springframework.cache.annotation.Cacheable;

public interface SignInService {
    Boolean signIn(Long userId);

    boolean judgeIsSign(String signInKey, int dayOfMonth);
}
