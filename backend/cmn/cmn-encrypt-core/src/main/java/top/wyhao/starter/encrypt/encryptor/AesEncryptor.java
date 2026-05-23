
package top.wyhao.starter.encrypt.encryptor;

import cn.hutool.crypto.symmetric.SymmetricAlgorithm;
import top.wyhao.starter.encrypt.context.CryptoContext;

/**
 * AES（Advanced Encryption Standard） 加密器
 * <p>
 * 美国国家标准与技术研究院(NIST)采纳的对称加密算法标准，提供128位、192位和256位三种密钥长度，以高效和安全性著称。
 * </p>
 *

 * @since 1.4.0
 */
public class AesEncryptor extends AbstractSymmetricCryptoEncryptor {

    public AesEncryptor(CryptoContext context) {
        super(context);
    }

    @Override
    protected SymmetricAlgorithm getAlgorithm() {
        return SymmetricAlgorithm.AES;
    }
}
