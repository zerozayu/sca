package com.zhangyu.sca.gateway.filter;

import cn.hutool.core.util.StrUtil;
import cn.hutool.jwt.JWTPayload;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.Payload;
import com.zhangyu.sca.common.result.ResultCode;
import com.zhangyu.sca.gateway.constants.RedisConstants;
import com.zhangyu.sca.gateway.util.WebFluxUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHeaders;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.text.ParseException;

/**
 * Token 验证全局过滤器，验证 token 的有效性
 *
 * @author zhangyu
 * @date 2024/9/25 15:23
 */
@Component
@Slf4j
@AllArgsConstructor
public class TokenAuthenticationFilter implements GlobalFilter, Ordered {

    private final StringRedisTemplate redisTemplate;
    private static final String BEARER_PREFIX = "Bearer ";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();

        String authorization = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (StrUtil.isBlank(authorization) && StrUtil.startWithIgnoreCase(authorization, BEARER_PREFIX)) {
            return chain.filter(exchange);
        }

        try {
            String token = authorization.substring(BEARER_PREFIX.length());
            JWSObject jwsObject = JWSObject.parse(token);
            Payload payload = jwsObject.getPayload();
            String jti = (String) jwsObject.getPayload().toJSONObject().get(JWTPayload.JWT_ID);
            Boolean isBlackToken = redisTemplate.hasKey(RedisConstants.TOKEN_BLACKLIST_PREFIX + jti);
            if (Boolean.TRUE.equals(isBlackToken)) {
                return WebFluxUtils.writeErrorResponse(response, ResultCode.TOKEN_ACCESS_FORBIDDEN);
            }

            // 为了避免业务应用自己还要去解析token，网关在将授权通过的请求转发给下游业务应用时，应该提前将token解析好，并放到请求头中，这样业务应用直接从请求头中获取用户信息
            ServerHttpRequest newRequest = request.mutate().header("user", payload.toString()).build();
            exchange = exchange.mutate().request(newRequest).build();
        } catch (ParseException e) {
            log.error("TokenAuthenticationFilter#filter: 解析 token 失败", e);
            return WebFluxUtils.writeErrorResponse(response, ResultCode.TOKEN_INVALID);
        }

        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return -100;
    }
}
