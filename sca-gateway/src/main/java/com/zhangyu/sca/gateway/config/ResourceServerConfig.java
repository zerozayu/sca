package com.zhangyu.sca.gateway.config;

import com.zhangyu.sca.gateway.properties.WhiteListProperties;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

/**
 * 资源服务器配置
 *
 * @author zhangyu
 * @date 2024/9/25 15:07
 */
@AllArgsConstructor
@Configuration
// 注解需要使用@EnableWebFluxSecurity而非@EnableWebSecurity,因为SpringCloud Gateway基于WebFlux
@EnableWebFluxSecurity
public class ResourceServerConfig {

    private final AuthorizationManager authorizationManager;
    private final CustomServerAccessDeniedHandler customServerAccessDeniedHandler;
    private final CustomServerAuthenticationEntryPoint customAuthenticationEntryPoint;
    private final WhiteListProperties whiteListProperties;

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {

        // 配置 HTTP 安全
        http
                // 资源服务器配置
                .oauth2ResourceServer(resourceServer -> resourceServer
                        .jwt(Customizer.withDefaults()))
                // 自定义处理 JWT 请求头过期或签名错误的结果
                .oauth2ResourceServer(resourceServer -> resourceServer
                        .authenticationEntryPoint(customAuthenticationEntryPoint))
                // 配置授权交换
                .authorizeExchange(exchange -> exchange
                        // 允许访问白名单中的 URL
                        .pathMatchers(ArrayUtils.toArray(whiteListProperties.getUrls()))
                        .permitAll()
                        // 其他所有请求需要通过授权管理器进行访问控制
                        .anyExchange().access(authorizationManager))
                // 配置异常处理
                .exceptionHandling(handling -> handling
                        // 配置访问被拒绝时的处理程序
                        .accessDeniedHandler(customServerAccessDeniedHandler)
                        // 配置未认证时的处理程序
                        .authenticationEntryPoint(customAuthenticationEntryPoint))
                // 禁用 CSRF 保护
                .csrf(ServerHttpSecurity.CsrfSpec::disable);
        return http.build();
    }
}
