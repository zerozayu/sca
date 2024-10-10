package com.zhangyu.sca.auth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
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

    /**
     * UserDetailsManager 继承了 UserDetailsService，
     * 除了加载用户信息外，还提供了用户管理的功能（创建、更新、删除用户等），适用于需要更全面用户管理的系统。
     *
     * @return
     */
    @Bean
    public UserDetailsManager userDetailsManager() {
        return new JdbcUserDetailsManager(dataSource);
    }

    /**
     * • 核心接口：AuthenticationManager 是 Spring Security 中的核心接口，用于管理认证的整个流程。
     * • 认证工作流：它接收 Authentication 对象，委托给一个或多个 AuthenticationProvider 进行认证。
     * • 扩展性：通过 AuthenticationManager，可以支持多种认证方式（如用户名/密码、OAuth2、JWT 等），并灵活扩展。
     * @param authConfig
     * @return
     * @throws Exception
     */
    // @Bean
    // public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
    //     return authConfig.getAuthenticationManager();
    // }

    /**
     * 	• 如果你提供了自定义的 UserDetailsService，Spring 会自动将其应用到默认的 DaoAuthenticationProvider。
     * 	• 如果你提供了自定义的 PasswordEncoder，Spring 会使用你提供的编码器。
     * 	• 如果没有自定义这两者，Spring 使用默认的内存用户和 bcrypt 加密算法。
     */
    // @Bean
    // public DaoAuthenticationProvider authenticationProvider() {
    //     DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
    //     authProvider.setUserDetailsService(userDetailsService); // 设置自定义的 UserDetailsService
    //     authProvider.setPasswordEncoder(bCryptPasswordEncoder()); // 设置密码编码器
    //     return authProvider;
    // }
}
