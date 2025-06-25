package com.zhangyu.sca.auth.config;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
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
import org.springframework.security.web.session.HttpSessionEventPublisher;
import org.springframework.security.web.util.matcher.MediaTypeRequestMatcher;

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
// @Import(OAuth2AuthorizationServerConfiguration.class)
public class AuthorizationServerConfig {

    private static final String CUSTOM_CONSENT_PAGE_URI = "/oauth2/consent";

    @Resource
    private KeyPair keyPair;
    @Lazy
    @Resource
    private OAuth2AuthorizationService authorizationService;
    @Lazy
    @Resource
    private RegisteredClientRepository registeredClientRepository;
    @Lazy
    @Resource
    private OAuth2AuthorizationConsentService authorizationConsentService;

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
        // 定义授权配置服务器
        OAuth2AuthorizationServerConfigurer authorizationServerConfigurer = new OAuth2AuthorizationServerConfigurer();
        authorizationServerConfigurer
                // 自定义授权页面  /oauth2/**
                .authorizationEndpoint(oAuth2AuthorizationEndpointConfigurer ->
                        oAuth2AuthorizationEndpointConfigurer.consentPage(CUSTOM_CONSENT_PAGE_URI))
                .oidc(Customizer.withDefaults());

        // 获取授权服务器相关的请求端点
        http
                // 获取授权服务器相关的请求端点
                .securityMatcher(authorizationServerConfigurer.getEndpointsMatcher())
                .with(authorizationServerConfigurer, (authorizationServer) ->
                        authorizationServer
                                .oidc(Customizer.withDefaults())
                                // 注册客户端存储库
                                .registeredClientRepository(registeredClientRepository)
                                // 配置 OAuth2 授权服务
                                .authorizationService(authorizationService)
                                // 配置 OAuth2 授权同意服务
                                .authorizationConsentService(authorizationConsentService)
                )
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
        ;

        return http.build();
    }

    /**
     * 配置会话注册中心
     * 如果启用了 OpenID Connect 1.0，SessionRegistry则会使用一个实例来跟踪已验证的会话。
     * SessionRegistry的默认实现会使用SessionAuthenticationStrategy与OAuth2 授权端点关联的实例来注册新的已验证会话。
     * 如果正在使用Spring Security 的并发会话控制功能，建议注册SessionRegistry @Bean以确保它在 Spring Security 的并发会话控制和 Spring Authorization Server 的注销功能之间共享。
     *
     * @return
     */
    @Bean
    public SessionRegistry sessionRegistry() {
        return new SessionRegistryImpl();
    }

    /**
     * 如果SessionRegistry @Bean已注册并且是的实例SessionRegistryImpl，
     * 则也HttpSessionEventPublisher @Bean 应该注册，因为它负责通知SessionRegistryImpl会话生命周期事件，
     * 例如，SessionDestroyedEvent以提供删除实例的能力SessionInformation。
     *
     * @return
     */
    @Bean
    public HttpSessionEventPublisher httpSessionEventPublisher() {
        return new HttpSessionEventPublisher();
    }

    /**
     * 用于管理客户端的实例。
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
     * 用于签署访问令牌的实例。
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
