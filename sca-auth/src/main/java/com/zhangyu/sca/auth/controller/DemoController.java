package com.zhangyu.sca.auth.controller;

import jakarta.annotation.security.PermitAll;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/demo")
public class DemoController {
    /**
     * PermitAll，等价于#permitAll() 方法，所有用户均可访问
     * 但是JavaConfig 配置的权限，和注解配置的权限，两者是叠加的，因为在 SecurityConfig 中配置了.anyRequest().authenticated()，
     * 所以此处的@PermitAll 注解并不会生效
     *
     * @return
     */
    @PermitAll
    @GetMapping("/echo")
    public String demo() {
        return "示例返回";
    }

    @GetMapping("/home")
    public String home() {
        return "我是首页";
    }

    /**
     * @return
     * @PreAuthorize 注解，等价于 #access(String attribute) 方法，，当 Spring EL 表达式的执行结果为 true 时，可以访问。
     */
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/admin")
    public String admin() {
        return "我是管理员";
    }

    @PreAuthorize("hasRole('ROLE_NORMAL')")
    @GetMapping("/normal")
    public String normal() {
        return "我是普通用户";
    }

}
