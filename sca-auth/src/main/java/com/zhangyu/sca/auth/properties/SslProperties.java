package com.zhangyu.sca.auth.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Ssl的配置类
 *
 * @author zhangyu
 * @date 2024/7/30 14:19
 */
@Component
@ConfigurationProperties(prefix = "server.ssl")
@Data
public class SslProperties {
    /**
     * location
     */
    private String keyStore;

    /**
     * password
     */
    private String keyStorePassword;

    /**
     * format
     */
    private String ketStoreType;
}
