package com.zhangyu.sca.auth.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.util.Set;

/**
 * 客户端添加请求实体
 *
 * @author zhangyu
 * @date 2024/6/21 14:05
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class ClientIdSaveReqDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 客户端 ID
     */
    private String clientId;

    /**
     * 密钥
     */
    private String secret;

    /**
     * 授权范围
     */
    private Set<String> scopes;
}
