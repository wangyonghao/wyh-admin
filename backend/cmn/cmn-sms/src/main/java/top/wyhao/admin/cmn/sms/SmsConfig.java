package top.wyhao.admin.cmn.sms;

import lombok.Data;
import top.wyhao.starter.core.enums.StatusEnum;

import java.util.Map;

/**
 * 短信配置接口
 *
 * @since 2026/5/18
 */
@Data
public class SmsConfig {

    /**
     * 获取配置 ID
     */
    private String id;

    /**
     * 获取配置名称
     */
    private String name;

    /**
     * 获取供应商
     */
    private String supplier;

    /**
     * 获取 Access Key
     */
    private String accessKey;

    /**
     * 获取 Secret Key
     */
    private String secretKey;

    /**
     * 获取短信签名
     */
    private String signature;

    /**
     * 获取模板 ID
     */
    private String templateId;

    /**
     * 获取负载均衡权重
     */
    private Integer weight;

    /**
     * 获取重试间隔（单位：秒）
     */
    private Integer retryInterval=5;

    /**
     * 获取重试次数
     */
    private Integer maxRetries=0;

    /**
     * 获取供应商独立配置
     */
    private Map<String, String> supplierConfig;

    /**
     * 获取状态
     */
    private StatusEnum status;
}
