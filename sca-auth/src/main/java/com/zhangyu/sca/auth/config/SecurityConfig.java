package com.zhangyu.sca.auth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

/**
 * spring security配置类
 *
 * @author zhangyu
 * @date 2024/5/20 11:52
 */
@EnableWebSecurity
@Configuration(proxyBeanMethods = false)
public class SecurityConfig {

    /**
     * 用于身份验证的
     *
     * @param http
     * @return {@link SecurityFilterChain}
     * @throws
     * @author zhangyu
     * @date 2024/6/6 09:57
     */
    @Bean
    public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests((authorize -> authorize
                        .requestMatchers("/login", "/error", "/logout").permitAll()
                        .anyRequest().authenticated())
                )
                .formLogin(Customizer.withDefaults());
        return http.build();
    }

    /**
     * 初始化加密对象
     * 此对象提供了一种不可逆的加密方式，相对于md5方式会更加安全
     */
    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        UserDetails user = User.builder()
                .username("zhangyu")
                .password("123456")
                .passwordEncoder(password -> bCryptPasswordEncoder().encode(password))
                .roles("USER")
                .build();

        return new InMemoryUserDetailsManager(user);
    }
}
