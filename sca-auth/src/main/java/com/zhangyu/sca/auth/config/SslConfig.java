package com.zhangyu.sca.auth.config;

import com.zhangyu.sca.auth.properties.SslProperties;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ResourceLoader;

import java.io.InputStream;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.Certificate;

/**
 * @author zhangyu
 * @date 2024/7/30 14:03
 */
@Configuration
@AllArgsConstructor
public class SslConfig {

    private final ResourceLoader resourceLoader;
    private final SslProperties sslProperties;

    // @Bean
    // public KeyPair keyPair1() {
    //     KeyStoreKeyFactory keyStoreKeyFactory = new KeyStoreKeyFactory(new ClassPathResource("jwt.jks"), "123456".toCharArray());
    //     return keyStoreKeyFactory.getKeyPair("jwt", "123456".toCharArray());
    // }

    @Bean
    public KeyPair keyPair() {
        return generateRsaKey();
    }

    private KeyPair generateRsaKey() {
        KeyPair keyPair;
        try {
            // 制定.p12文件路径及密码
            // String p12FilePath = "classpath:/keystore/keystore.p12";
            // String p12FilePassword = "123456";
            String p12FilePath = sslProperties.getKeyStore();
            String p12FilePassword = sslProperties.getKeyStorePassword();
            char[] password = p12FilePassword.toCharArray();

            // 获取密钥库实例
            KeyStore keyStore = KeyStore.getInstance("PKCS12");
            // 加载
            // try (InputStream inputStream = ClassPathResource.class.getClassLoader().getResourceAsStream(p12FilePath)) {
            try (InputStream inputStream = resourceLoader.getResource(p12FilePath).getInputStream()) {
                keyStore.load(inputStream, password);
            }

            // 获取别名
            String alias = keyStore.aliases().nextElement();

            // 获取私钥
            PrivateKey privateKey = (PrivateKey) keyStore.getKey(alias, password);

            // 获取证书
            Certificate certificate = keyStore.getCertificate(alias);

            // 从证书中获取公钥
            PublicKey publicKey = certificate.getPublicKey();

            keyPair = new KeyPair(publicKey, privateKey);
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
        return keyPair;
    }
}
