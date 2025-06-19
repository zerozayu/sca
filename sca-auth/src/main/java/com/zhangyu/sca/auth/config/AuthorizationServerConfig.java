package com.zhangyu.sca.auth.config;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.authorization.JdbcOAuth2AuthorizationConsentService;
import org.springframework.security.oauth2.server.authorization.JdbcOAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationConsentService;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.client.JdbcRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.util.matcher.MediaTypeRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

import java.security.KeyPair;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.UUID;

/**
 * Authorization Server 配置类
 *
 * @author zhangyu
 * @date 2024/6/12 15:15
 */
@Configuration(proxyBeanMethods = false)
@AllArgsConstructor
// @Import(OAuth2AuthorizationServerConfiguration.class)
public class AuthorizationServerConfig {

    private static final String CUSTOM_CONSENT_PAGE_URI = "/oauth2/consent";

    private final KeyPair keyPair;

    /***
     * 针对协议端点的 Spring Security 过滤器链
     *
     * @param http
     * @return {@link SecurityFilterChain}
     * @throws
     * @author zhangyu
     * @date 2024/6/6 09:55
     */
    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http) throws Exception {

        // OAuth2AuthorizationServerConfiguration.applyDefaultSecurity(http);
        // http.getConfigurer(OAuth2AuthorizationServerConfigurer.class)
        //         .oidc(Customizer.withDefaults()); //配置 OpenID Connect 1.0 支持

        // 定义授权配置服务器
        OAuth2AuthorizationServerConfigurer configurer = new OAuth2AuthorizationServerConfigurer();
        configurer
                // 自定义授权页面  / oauth2/**
                .authorizationEndpoint(oAuth2AuthorizationEndpointConfigurer ->
                        oAuth2AuthorizationEndpointConfigurer.consentPage(CUSTOM_CONSENT_PAGE_URI))
                .oidc(Customizer.withDefaults());

        // 获取授权服务器相关的请求端点
        RequestMatcher endpointsMatcher = configurer.getEndpointsMatcher();

        http
                // 拦截对授权服务器相关端点的请求
                .securityMatcher(endpointsMatcher)
                // 拦截到的请求都需要认证
                .authorizeHttpRequests(authorizationManagerRequestMatcherRegistry -> authorizationManagerRequestMatcherRegistry
                        .anyRequest().authenticated())
                // 忽略掉相关端点的 CSRF（跨站请求）：对授权端点的访问是可以跨站的
                .csrf(csrf -> csrf.ignoringRequestMatchers(endpointsMatcher))
                .oauth2ResourceServer(resourceServer -> resourceServer
                        .jwt(Customizer.withDefaults()))
                .exceptionHandling((exceptions) -> exceptions
                        .defaultAuthenticationEntryPointFor(
                                new LoginUrlAuthenticationEntryPoint("/login"),
                                new MediaTypeRequestMatcher(MediaType.TEXT_HTML)
                        )
                )
                // 访问端点时表单登录
                .formLogin(Customizer.withDefaults())
                // 应用授权服务器的配置
                .with(configurer, Customizer.withDefaults());

        return http.build();
    }

    /**
     * 注册客户端应用, 对应 oauth2_registered_client 表
     *
     * @param jdbcTemplate
     * @return {@link RegisteredClientRepository}
     * @throws
     * @author zhangyu
     * @date 2024/6/12 17:35
     */
    @Bean
    public RegisteredClientRepository registeredClientRepository(JdbcTemplate jdbcTemplate) {
        return new JdbcRegisteredClientRepository(jdbcTemplate);
    }

    /**
     * 令牌的发放记录, 对应 oauth2_authorization 表
     *
     * @param jdbcTemplate
     * @param registeredClientRepository
     * @return {@link OAuth2AuthorizationService}
     * @throws
     * @author zhangyu
     * @date 2024/6/12 17:37
     */
    @Bean
    public OAuth2AuthorizationService authorizationService(JdbcTemplate jdbcTemplate, RegisteredClientRepository registeredClientRepository) {
        return new JdbcOAuth2AuthorizationService(jdbcTemplate, registeredClientRepository);
    }

    /**
     * 把资源拥有者授权确认操作保存到数据库, 对应 oauth2_authorization_consent 表
     *
     * @param jdbcTemplate
     * @param registeredClientRepository
     * @return {@link OAuth2AuthorizationConsentService}
     * @throws
     * @author zhangyu
     * @date 2024/6/12 17:39
     */
    @Bean
    public OAuth2AuthorizationConsentService authorizationConsentService(JdbcTemplate jdbcTemplate, RegisteredClientRepository registeredClientRepository) {
        return new JdbcOAuth2AuthorizationConsentService(jdbcTemplate, registeredClientRepository);
    }

    /**
     * 加载 JWT 资源, 用于生成令牌
     *
     * @param
     * @return {@link JWKSource<SecurityContext>}
     * @throws
     * @author zhangyu
     * @date 2024/6/12 15:29
     */
    @Bean
    public JWKSource<SecurityContext> jwkSource() {
        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
        RSAKey rsaKey = new RSAKey.Builder(publicKey)
                .privateKey(privateKey)
                .keyID(UUID.randomUUID().toString())
                .build();
        JWKSet jwkSet = new JWKSet(rsaKey);
        // return new ImmutableJWKSet<>(jwkSet);
        return (jwtSelector, securityContext) -> jwtSelector.select(jwkSet);
    }

    /**
     * JWT 解码
     *
     * @param jwkSource
     * @return {@link JwtDecoder}
     * @throws
     * @author zhangyu
     * @date 2024/6/12 15:27
     */
    @Bean
    public JwtDecoder jwtDecoder(JWKSource<SecurityContext> jwkSource) {
        return OAuth2AuthorizationServerConfiguration.jwtDecoder(jwkSource);
    }

    /**
     * AuthorizationServerSettings 的相关配置
     *
     * @param
     * @return {@link AuthorizationServerSettings}
     * @throws
     * @author zhangyu
     * @date 2024/6/12 15:28
     */
    @Bean
    public AuthorizationServerSettings authorizationServerSettings() {
        return AuthorizationServerSettings.builder().build();
    }
}
