
package top.wyhao.starter.encrypt.encryptor;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.crypto.symmetric.SymmetricAlgorithm;
import cn.hutool.crypto.symmetric.SymmetricCrypto;
import top.wyhao.starter.core.constant.StringConstants;
import top.wyhao.starter.encrypt.context.CryptoContext;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 对称加密器
 *


 * @since 1.4.0
 */
public abstract class AbstractSymmetricCryptoEncryptor extends AbstractEncryptor {

    /**
     * 对称加密缓存
     */
    private static final Map<String, SymmetricCrypto> CACHE = new ConcurrentHashMap<>();

    /**
     * 加密上下文
     */
    private final CryptoContext context;

    protected AbstractSymmetricCryptoEncryptor(CryptoContext context) {
        super(context);
        this.context = context;
    }

    @Override
    public String encrypt(String plaintext) {
        if (CharSequenceUtil.isBlank(plaintext)) {
            return plaintext;
        }
        return this.getCrypto(context.getPassword()).encryptHex(plaintext);
    }

    @Override
    public String decrypt(String ciphertext) {
        if (CharSequenceUtil.isBlank(ciphertext)) {
            return ciphertext;
        }
        return this.getCrypto(context.getPassword()).decryptStr(ciphertext);
    }

    /**
     * 获取对称加密算法
     *
     * @param password 密钥
     * @return 对称加密算法
     */
    protected SymmetricCrypto getCrypto(String password) {
        SymmetricAlgorithm algorithm = this.getAlgorithm();
        String key = algorithm + StringConstants.UNDERLINE + password;
        if (CACHE.containsKey(key)) {
            return CACHE.get(key);
        }
        SymmetricCrypto symmetricCrypto = new SymmetricCrypto(algorithm, password.getBytes(StandardCharsets.UTF_8));
        CACHE.put(key, symmetricCrypto);
        return symmetricCrypto;
    }

    /**
     * 获取对称加密算法类型
     *
     * @return 对称加密算法类型
     */
    protected abstract SymmetricAlgorithm getAlgorithm();
}
