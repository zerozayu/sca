package com.zhangyu.sca.auth.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * @author zhangyu
 * @date 2024/6/20 16:13
 */
@Controller
public class LoginController {

    @GetMapping("/login")
    public String login() {
        return "login.html";
    }
}
