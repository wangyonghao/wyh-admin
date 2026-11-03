package top.wyhao.mybatis.encrypt;

import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.symmetric.AES;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;

import java.nio.charset.StandardCharsets;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/// 数据库字段加解密 TypeHandler
/// 自动注册需配合 @Component + mybatis-plus.type-handlers-package 配置
///
/// 在需要加解密的字段上使用 @TableField(typeHandler = ...)。
/// 建议在 @TableName 上开启 autoResultMap = true，确保 MP 正确生成 ResultMap 并触发 TypeHandler。
/// 使用示例：
/// ```
/// @TableName(value = "sys_user", autoResultMap = true)
/// public class User {
///     // 敏感字段：身份证、手机号等
///     @TableField(typeHandler = EncryptTypeHandler.class)
///     private String idCard;
///     @TableField(typeHandler = EncryptTypeHandler.class)
///     private String phone;
/// }
/// ```
///
@MappedTypes(String.class)
@MappedJdbcTypes(JdbcType.VARCHAR)
public class EncryptTypeHandler extends BaseTypeHandler<String> {

    // 使用 volatile + 双重检查锁保证线程安全与延迟初始化
    private static volatile AES AES_INSTANCE;

    /**
     * 由 Spring 配置类调用，初始化密钥
     */
    public static synchronized void init(String aesKey) {
        if (null == aesKey || aesKey.isEmpty()) {
            throw new IllegalArgumentException("EncryptTypeHandler - 未配置 application.crypto.aes-key");
        }
        // AES 密钥长度校验（16/24/32 字节）
        int len = aesKey.getBytes(StandardCharsets.UTF_8).length;
        if (len != 16 && len != 24 && len != 32) {
            throw new IllegalArgumentException("EncryptTypeHandler - AES密钥长度非法：必须为 16/24/32 字节，当前为 " + len);
        }

        if (AES_INSTANCE == null) {
            AES_INSTANCE = SecureUtil.aes(aesKey.getBytes(StandardCharsets.UTF_8));
        }
    }

    private AES getAes() {
        if (AES_INSTANCE == null) {
            throw new IllegalStateException("EncryptTypeHandler - AES加密密钥未初始化，请检查 app.crypto.aes-key 配置或环境变量");
        }
        return AES_INSTANCE;
    }


    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, String parameter, JdbcType jdbcType) throws SQLException {
        // 写入时加密
        ps.setString(i, this.encrypt(parameter));
    }

    @Override
    public String getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String value = rs.getString(columnName);
        return decrypt(value);
    }

    @Override
    public String getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String value = rs.getString(columnIndex);
        return decrypt(value);
    }

    @Override
    public String getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String value = cs.getString(columnIndex);
        return decrypt(value);
    }

    private String encrypt(String value) {
        return this.getAes().encryptBase64(value);
    }

    private String decrypt(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            // 兼容历史明文数据：若解密失败则返回原值（可根据业务改为抛异常或日志告警）
            return this.getAes().decryptStr(value);
        } catch (Exception e) {
            // 记录日志或监控告警
            return value;
        }
    }
}
