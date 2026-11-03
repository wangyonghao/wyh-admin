
package top.wyhao.starter.encrypt.encryptor;

import cn.hutool.core.codec.Base64;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.asymmetric.KeyType;
import top.wyhao.starter.encrypt.context.CryptoContext;

/**
 * RSA 加密器
 * <p>
 * 非对称加密算法，由罗纳德·李维斯特（Ron Rivest）、阿迪·沙米尔（Adi Shamir）和伦纳德·阿德曼（Leonard Adleman）于1977年提出，安全性基于大数因子分解问题的困难性。
 * </p>
 *


 * @since 1.4.0
 */
public class RsaEncryptor extends AbstractEncryptor {

    /**
     * 加密上下文
     */
    private final CryptoContext context;

    public RsaEncryptor(CryptoContext context) {
        super(context);
        this.context = context;
    }

    @Override
    public String encrypt(String plaintext) {
        return Base64.encode(SecureUtil.rsa(null, context.getPublicKey()).encrypt(plaintext, KeyType.PublicKey));
    }

    @Override
    public String decrypt(String ciphertext) {
        return new String(SecureUtil.rsa(context.getPrivateKey(), null)
            .decrypt(Base64.decode(ciphertext), KeyType.PrivateKey));
    }
}
