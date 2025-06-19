package com.zhangyu.sca.auth.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.security.oauth2.core.oidc.OidcScopes;

import java.util.HashMap;
import java.util.Map;

/**
 * 数据权限类型枚举类
 *
 * @author zhangyu
 * @date 2024/6/20 17:49
 */
@Getter
@AllArgsConstructor
public enum ScopeWithDescription {
    PROFILE(OidcScopes.PROFILE, "此应用程序将能够读取您的个人资料信息"),
    MESSAGE_READ("message.read", "此应用程序将能够读取您的信息"),
    MESSAGE_WRITE("message.write", "此应用程序将能够添加新信息, 它还可以编辑和删除现有信息"),
    OTHER_SCOPE("other.scope", "这是范围描述的另一个范围示例"),
    DEFAULT_DESCRIPTION("default", "未知范围 - 我们无法提供有关此权限的信息, 请在授予此权限时谨慎");
    private static final Map<String, ScopeWithDescription> scopeDescriptions = new HashMap<>();

    static {
        for (ScopeWithDescription scope : ScopeWithDescription.values()) {
            scopeDescriptions.put(scope.scope, scope);
        }
    }

    private final String scope;
    private final String description;

    public static ScopeWithDescription getByScope(String scope) {
        return scopeDescriptions.getOrDefault(scope, DEFAULT_DESCRIPTION);
    }
}
