package com.zhangyu.sca.auth.controller;

import com.zhangyu.sca.auth.domain.dto.ClientIdSaveReqDTO;
import com.zhangyu.sca.auth.domain.dto.UserSaveReqDTO;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * 用于添加用户信息和客户端信息
 *
 * @author zhangyu
 * @date 2024/6/21 11:42
 */
@RestController
@RequestMapping
public class ServerController {

    public ServerController(UserDetailsManager userDetailsManager, BCryptPasswordEncoder bCryptPasswordEncoder, RegisteredClientRepository registeredClientRepository) {
        this.userDetailsManager = userDetailsManager;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.registeredClientRepository = registeredClientRepository;
    }

    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final UserDetailsManager userDetailsManager;

    @PostMapping(value = "/api/add_user")
    public String addUser(@Validated @RequestBody UserSaveReqDTO userSaveReqDTO) {
        UserDetails userDetails = User.builder()
                .passwordEncoder(bCryptPasswordEncoder::encode)
                .username(userSaveReqDTO.getUsername())
                .password(userSaveReqDTO.getPassword())
                .roles("USER")
                .build();
        userDetailsManager.createUser(userDetails);
        System.out.println(1111);
        return "添加用户成功";
    }

    private final RegisteredClientRepository registeredClientRepository;

    @PostMapping(value = "/api/add_client")
    public String addClient(@RequestBody ClientIdSaveReqDTO clientIdSaveReqDTO) {
        // JWT 的配置项：TTL、是否复用 refreshToken 等等
        TokenSettings tokenSettings = TokenSettings.builder()
                // 令牌存活时间：2小时
                .accessTokenTimeToLive(Duration.ofHours(2))
                // 令牌可以刷新，重新获取
                .reuseRefreshTokens(true)
                // 刷新令牌存货时间：30天
                .refreshTokenTimeToLive(Duration.ofDays(30))
                .build();
        // 客户端相关配置
        ClientSettings clientSettings = ClientSettings.builder()
                // 是否需要用户授权确认
                .requireAuthorizationConsent(true)
                .build();

        String clientId = clientIdSaveReqDTO.getClientId();
        String secret = clientIdSaveReqDTO.getSecret();
        Set<String> scopes = clientIdSaveReqDTO.getScopes();
        RegisteredClient registeredClient = RegisteredClient.withId(UUID.randomUUID().toString())
                // 客户端 ID 和密码
                .clientId(clientId)
                .clientSecret(bCryptPasswordEncoder.encode(secret))
                // 授权方法
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantTypes((typeSet) -> {
                    // 授权模式（授权码模式）
                    typeSet.add(AuthorizationGrantType.AUTHORIZATION_CODE);
                    // 刷新令牌（授权码模式）
                    typeSet.add(AuthorizationGrantType.REFRESH_TOKEN);
                    typeSet.add(AuthorizationGrantType.CLIENT_CREDENTIALS);
                })
                // 回调地址：授权服务器向当前客户端响应时调用下面地址，不在此列的地址将被拒绝，只能使用 IP 或域名，不能使用 localhost
                .redirectUri("https://127.0.0.1:41000/auth/login/oauth2/code/" + clientId + "-oidc")
                // oidc 支持
                .scope(OidcScopes.OPENID)
                .scope(OidcScopes.PROFILE)
                // 授权范围（当前客户端的授权范围）
                .scopes(scopesSet -> scopesSet.addAll(scopes))
                // JWT 配置项
                .tokenSettings(tokenSettings)
                // 客户端配置项
                .clientSettings(clientSettings)
                .build();

        registeredClientRepository.save(registeredClient);
        return "添加客户端信息成功";
    }

}
