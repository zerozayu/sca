package com.zhangyu.sca.auth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

import javax.sql.DataSource;

/**
 * spring security配置类
 *
 * @author zhangyu
 * @date 2024/5/20 11:52
 */
@EnableWebSecurity
@Configuration
public class SecurityConfig {

    private final DataSource dataSource;

    public SecurityConfig(DataSource dataSource) {
        this.dataSource = dataSource;
    }

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
                .csrf(AbstractHttpConfigurer::disable)
                .cors(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests((authorize -> authorize
                                // 配置放行的请求
                                .requestMatchers("/login").permitAll()
                                .requestMatchers("/api/**").permitAll()
                                // 其他的任何请求都需要认证
                                .anyRequest().authenticated()
                        )
                )
                // 设置登录表单页面
                .formLogin(formLoginConfigurer -> formLoginConfigurer.loginPage("/login"));
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

    // @Bean
    // public UserDetailsService userDetailsService() {
    //     UserDetails user = User.builder()
    //             .username("zhangyu")
    //             .password("123456")
    //             .passwordEncoder(password -> bCryptPasswordEncoder().encode(password))
    //             .roles("USER")
    //             .build();
    //
    //     return new InMemoryUserDetailsManager(user);
    // }

    @Bean
    public UserDetailsManager userDetailsManager() {
        return new JdbcUserDetailsManager(dataSource);
    }
}
