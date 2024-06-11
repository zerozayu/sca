package com.zhangyu.sca.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 统一认证中心主启动类
 *
 * @author zhangyu
 * @date 2024/5/20 09:28
 */
@SpringBootApplication
public class ScaAuthApplication {
    public static void main(String[] args) {
        SpringApplication.run(ScaAuthApplication.class, args);
        System.out.println("=============sca-auth started.=============");
    }
}
