package com.zhangyu.sca.gateway.config;

import com.zhangyu.sca.common.result.ResultCode;
import com.zhangyu.sca.gateway.util.WebFluxUtils;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.server.authorization.ServerAccessDeniedHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * 无权访问自定义响应
 *
 * @author zhangyu
 * @date 2024/9/25 17:29
 */
@Component
public class CustomServerAccessDeniedHandler implements ServerAccessDeniedHandler {
    @Override
    public Mono<Void> handle(ServerWebExchange exchange, AccessDeniedException denied) {
        ServerHttpResponse response = exchange.getResponse();
        return WebFluxUtils.writeErrorResponse(response, ResultCode.ACCESS_UNAUTHORIZED);
    }
}
