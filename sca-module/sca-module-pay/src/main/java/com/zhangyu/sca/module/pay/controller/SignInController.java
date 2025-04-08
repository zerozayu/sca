package com.zhangyu.sca.module.pay.controller;

import com.zhangyu.sca.module.pay.service.SignInService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 签到
 */
@RestController
@RequestMapping("/sign-in")
@AllArgsConstructor
public class SignInController {

    private final SignInService signInService;

    @GetMapping
    public Boolean signIn() {
        // todo: 获取当前登录用户
        Long userId = 123L;
        return signInService.signIn(userId);
    }
}
