
package top.wyhao.starter.encrypt.encryptor;

import top.wyhao.starter.encrypt.context.CryptoContext;

/**
 * 加密器基类
 *

 * @since 2.13.2
 */
public abstract class AbstractEncryptor implements IEncryptor {

    protected AbstractEncryptor(CryptoContext context) {
        // 配置校验与配置注入
    }

}
