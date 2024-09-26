package com.zhangyu.sca.gateway.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 白名单配置类
 *
 * @author zhangyu
 * @date 2024/7/30 14:55
 */
@Component
@ConfigurationProperties(prefix = "white-list")
@Data
public class WhiteListProperties {

    /**
     * 白名单urls
     */
    private String[] urls;
}
