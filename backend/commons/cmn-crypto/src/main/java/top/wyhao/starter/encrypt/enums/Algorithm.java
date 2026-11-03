
package top.wyhao.starter.encrypt.enums;

import top.wyhao.starter.encrypt.encryptor.*;

/**
 * 加密算法枚举
 *


 * @since 1.4.0
 */
public enum Algorithm {

    /**
     * 默认使用配置属性的算法
     */
    DEFAULT(null),

    /**
     * AES
     */
    AES(AesEncryptor.class),

    /**
     * DES
     */
    DES(DesEncryptor.class),

    /**
     * PBE With MD5 And DES
     */
    PBE_WITH_MD5_AND_DES(PbeWithMd5AndDesEncryptor.class),

    /**
     * RSA
     */
    RSA(RsaEncryptor.class),

    /**
     * Base64
     */
    BASE64(Base64Encryptor.class);

    /**
     * 加密/解密处理器
     */
    private final Class<? extends IEncryptor> encryptor;

    Algorithm(Class<? extends IEncryptor> encryptor) {
        this.encryptor = encryptor;
    }

    public Class<? extends IEncryptor> getEncryptor() {
        return encryptor;
    }
}
