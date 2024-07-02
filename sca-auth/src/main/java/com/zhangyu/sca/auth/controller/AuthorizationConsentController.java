package com.zhangyu.sca.auth.controller;

import com.zhangyu.sca.auth.domain.ScopeWithDescription;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationConsent;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationConsentService;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * @author zhangyu
 * @date 2024/6/20 16:18
 */
@Controller
public class AuthorizationConsentController {
    private final RegisteredClientRepository registeredClientRepository;
    protected final OAuth2AuthorizationConsentService oAuth2AuthorizationConsentService;

    public AuthorizationConsentController(RegisteredClientRepository registeredClientRepository, OAuth2AuthorizationConsentService oAuth2AuthorizationConsentService) {
        this.registeredClientRepository = registeredClientRepository;
        this.oAuth2AuthorizationConsentService = oAuth2AuthorizationConsentService;
    }

    @GetMapping(value = "/oauth2/consent")
    public String consent(Principal principal, Model model,
                          @RequestParam(OAuth2ParameterNames.CLIENT_ID) String clientId,
                          @RequestParam(OAuth2ParameterNames.SCOPE) String scope,
                          @RequestParam(OAuth2ParameterNames.STATE) String state) {
        // 要批准的范围和以前批准的范围
        Set<String> scopesToApprove = new HashSet<>();
        Set<String> previouslyApprovedScopes = new HashSet<>();
        // 查询 clientId 是否存在
        RegisteredClient registeredClient =
                Optional.ofNullable(this.registeredClientRepository.findByClientId(clientId))
                        .orElseThrow(RuntimeException::new);
        // 查询当前的授权许可
        OAuth2AuthorizationConsent currentAuthorizationConsent = this.oAuth2AuthorizationConsentService.findById(registeredClient.getId(), principal.getName());
        Set<String> authorizedScopes =
                Optional.ofNullable(currentAuthorizationConsent)
                        .map(OAuth2AuthorizationConsent::getScopes)
                        .orElseGet(Collections::emptySet);

        for (String requestedScope : StringUtils.delimitedListToStringArray(scope, " ")) {
            if (OidcScopes.OPENID.equals(requestedScope)) {
                continue;
            }

            // 如果已授权范围包含了请求范围，则添加到以前批准的范围的 Set, 否则添加到要批准的范围
            if (authorizedScopes.contains(requestedScope)) {
                previouslyApprovedScopes.add(requestedScope);
            } else {
                scopesToApprove.add(requestedScope);
            }
        }

        model.addAttribute("clientId", clientId);
        model.addAttribute("state", state);
        model.addAttribute("scopes", withDescription(scopesToApprove));
        model.addAttribute("previouslyApprovedScopes", withDescription(previouslyApprovedScopes));
        model.addAttribute("principalName", principal.getName());

        return "consent";

    }

    private static Set<ScopeWithDescription> withDescription(Set<String> scopes) {
        Set<ScopeWithDescription> scopeWithDescriptions = new HashSet<>();
        for (String scope : scopes) {
            scopeWithDescriptions.add(ScopeWithDescription.getByScope(scope));
        }
        return scopeWithDescriptions;
    }
}
