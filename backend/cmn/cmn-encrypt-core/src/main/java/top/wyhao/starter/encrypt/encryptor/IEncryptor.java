
package top.wyhao.starter.encrypt.encryptor;

/**
 * 加密器接口
 *


 * @since 1.4.0
 */
public interface IEncryptor {

    /**
     * 加密
     *
     * @param plaintext 明文
     * @return 加密后的文本
     */
    String encrypt(String plaintext);

    /**
     * 解密
     *
     * @param ciphertext 密文
     * @return 解密后的文本
     */
    String decrypt(String ciphertext);
}
