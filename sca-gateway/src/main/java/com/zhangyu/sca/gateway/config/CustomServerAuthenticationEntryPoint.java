package com.zhangyu.sca.gateway.config;

import com.zhangyu.sca.common.result.ResultCode;
import com.zhangyu.sca.gateway.util.WebFluxUtils;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * 无效token/token过期 自定义响应
 *
 * @author zhangyu
 * @date 2024/9/25 16:31
 */
@Component
public class CustomServerAuthenticationEntryPoint implements ServerAuthenticationEntryPoint {

    @Override
    public Mono<Void> commence(ServerWebExchange exchange, AuthenticationException ex) {
        ServerHttpResponse response = exchange.getResponse();
        return WebFluxUtils.writeErrorResponse(response, ResultCode.TOKEN_INVALID);
    }
}
