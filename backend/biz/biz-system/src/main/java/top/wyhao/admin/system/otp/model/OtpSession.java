package top.wyhao.admin.system.otp.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import top.wyhao.admin.system.otp.enums.OtpChannel;
import top.wyhao.admin.system.otp.enums.OtpScene;

import java.io.Serializable;

/**
 * OTP 会话数据模型
 *

 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OtpSession implements Serializable {

    /**
     * 会话唯一标识（UUID）
     */
    private String uuid;

    /**
     * 发送渠道
     */
    private OtpChannel channel;

    /**
     * 业务场景
     */
    private OtpScene scene;

    /**
     * 目标地址（邮箱或手机号）
     */
    private String target;

    /**
     * 验证码
     */
    private String code;

    /**
     * 创建时间（时间戳，秒）
     */
    private Long createdAt;

    /**
     * 过期时间（时间戳，秒）
     */
    private Long expiresAt;

    /**
     * 是否已验证
     */
    private Boolean verified;

    /**
     * 验证失败次数
     */
    private Integer failCount;

    /**
     * 语言代码
     */
    private String locale;
}
