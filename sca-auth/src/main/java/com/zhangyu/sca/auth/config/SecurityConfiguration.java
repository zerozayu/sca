package com.zhangyu.sca.auth.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * spring security配置类
 *
 * @author zhangyu
 * @date 2024/5/20 11:52
 */
@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

    /**
     * 初始化加密对象
     * 此对象提供了一种不可逆的加密方式，相对于md5方式会更加安全
     */
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
                // 开启跨域
                .cors().and()
                // CSRF 禁用，应为不使用 Session
                .csrf().disable()
                // 基于 token 机制，所以不需要使用 Session
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
                .headers().frameOptions().disable();

        httpSecurity
                .authorizeHttpRequests()
                .requestMatchers("/register/**").permitAll()
                .requestMatchers("/index").permitAll()
                .requestMatchers("/users").hasRole("ADMIN")
        ;

        //3.定义登录成功和失败以后的处理逻辑(可选)
        //假如没有如下设置登录成功会显示404
        httpSecurity.formLogin() //这句话会对外暴露一个登录路径/login
                .successHandler(successHandler())
                .failureHandler(failureHandler());

        return httpSecurity.build();
    }

    //定义认证成功的处理器
    @Bean
    public AuthenticationSuccessHandler successHandler() {
//        return new AuthenticationSuccessHandler() {
//            @Override
//            public void onAuthenticationSuccess(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Authentication authentication) throws IOException, ServletException {
//
//            }
//        }
        //lambda表达式
        return (request, response, authentication) -> {
            Map<String, Object> map = new HashMap<>();
            map.put("state", 200);
            map.put("message", "login ok");
            //将map对象转换为json格式字符串并写到客户端
            writeJsonToClient(response, map);
        };
    }

    //定义认证失败的处理器
    @Bean
    public AuthenticationFailureHandler failureHandler() {
        return (request, response, authentication) -> {
            Map<String, Object> map = new HashMap<>();
            map.put("state", 500);
            map.put("message", "login failure");
            //将map对象转换为json格式字符串并写到客户端
            writeJsonToClient(response, map);
        };
    }

    private void writeJsonToClient(HttpServletResponse response, Map<String, Object> map) throws IOException {
        //1.将对象转换为json
        //将对象转换为json有3种方案:
        //1)Google的Gson-->toJson  (需要自己找依赖)
        //2)阿里的fastjson-->JSON (spring-cloud-starter-alibaba-sentinel)
        //3)Springboot web自带的jackson-->writeValueAsString (spring-boot-starter-web)
        //我们这里借助springboot工程中自带的jackson
        //jackson中有一个对象类型为ObjectMapper,它内部提供了将对象转换为json的方法
        //例如:
        String json = new ObjectMapper().writeValueAsString(map);
        response.setCharacterEncoding("utf-8");
        response.setContentType("application/json;charset=utf-8");
        PrintWriter out = response.getWriter();
        out.println(json);
        out.flush();
        out.close();
    }


}
