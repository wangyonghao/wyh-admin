
package top.wyhao.starter.encrypt.context;

import top.wyhao.starter.encrypt.encryptor.IEncryptor;
import top.wyhao.starter.encrypt.enums.Algorithm;

import java.util.Objects;

/**
 * 加密上下文
 *

 * @since 2.13.2
 */
public class CryptoContext {

    /**
     * 默认算法
     */
    private Algorithm algorithm;

    /**
     * 加密/解密处理器
     * <p>
     * 优先级高于加密/解密算法
     * </p>
     */
    Class<? extends IEncryptor> encryptor;

    /**
     * 对称加密算法密钥
     */
    private String password;

    /**
     * 非对称加密算法公钥
     */
    private String publicKey;

    /**
     * 非对称加密算法私钥
     */
    private String privateKey;

    public Algorithm getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(Algorithm algorithm) {
        this.algorithm = algorithm;
    }

    public Class<? extends IEncryptor> getEncryptor() {
        return encryptor;
    }

    public void setEncryptor(Class<? extends IEncryptor> encryptor) {
        this.encryptor = encryptor;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public String getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CryptoContext that = (CryptoContext)o;
        return algorithm == that.algorithm && Objects.equals(encryptor, that.encryptor) && Objects
            .equals(password, that.password) && Objects.equals(publicKey, that.publicKey) && Objects
                .equals(privateKey, that.privateKey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(algorithm, encryptor, password, publicKey, privateKey);
    }
}
