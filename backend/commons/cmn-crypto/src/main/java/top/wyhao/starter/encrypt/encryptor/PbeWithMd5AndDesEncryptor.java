
package top.wyhao.starter.encrypt.encryptor;

import cn.hutool.crypto.symmetric.SymmetricAlgorithm;
import top.wyhao.starter.encrypt.context.CryptoContext;

/**
 * PBEWithMD5AndDES（Password Based Encryption With MD5 And DES） 加密器
 * <p>
 * 混合加密算法，结合了 MD5 散列算法和 DES（Data Encryption Standard）加密算法
 * </p>
 *

 * @since 1.4.0
 */
public class PbeWithMd5AndDesEncryptor extends AbstractSymmetricCryptoEncryptor {

    public PbeWithMd5AndDesEncryptor(CryptoContext context) {
        super(context);
    }

    @Override
    protected SymmetricAlgorithm getAlgorithm() {
        return SymmetricAlgorithm.PBEWithMD5AndDES;
    }
}
