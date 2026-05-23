
package top.wyhao.admin.system.model.vo;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import top.wyhao.starter.core.enums.StatusEnum;
import top.wyhao.starter.excel.converter.ExcelBaseEnumConverter;
import top.wyhao.starter.web.sensitive.annotation.Sensitive;
import top.wyhao.starter.web.sensitive.enums.SensitiveMethod;
import top.wyhao.starter.web.excel.DictExcelProperty;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 短信配置响应参数
 *


 * @since 2025/03/15 18:41
 */
@Data
@ExcelIgnoreUnannotated
@Schema(description = "短信配置响应参数")
public class SmsConfigResult{
    /**
     * ID
     */
    @Schema(description = "ID", example = "1")
    private Long id;

    /**
     * 是否禁用修改
     */
    @Schema(description = "是否禁用修改", example = "true")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean disabled;

    /**
     * 名称
     */
    @Schema(description = "名称", example = "短信配置1")
    @ExcelProperty(value = "名称")
    private String name;

    /**
     * 厂商
     *
     * @see org.dromara.sms4j.comm.constant.SupplierConstant
     */
    @Schema(description = "厂商", example = "cloopen")
    @ExcelProperty(value = "厂商", converter = top.wyhao.starter.web.excel.ExcelDictConverter.class)
    @DictExcelProperty("sms_supplier")
    private String supplier;

    /**
     * Access Key
     */
    @Schema(description = "Access Key", example = "7aaf0708674db3ee05676ecbc2f31b7b")
    @ExcelProperty(value = "Access Key")
    private String accessKey;

    /**
     * Secret Key
     */
    @Schema(description = "Secret Key", example = "7fd4************************57be")
    @ExcelProperty(value = "Secret Key")
    @Sensitive(SensitiveMethod.SECRET_KEY)
    private String secretKey;

    /**
     * 短信签名
     */
    @Schema(description = "短信签名")
    @ExcelProperty(value = "短信签名")
    private String signature;

    /**
     * 模板 ID
     */
    @Schema(description = "模板 ID", example = "1")
    @ExcelProperty(value = "模板 ID")
    private String templateId;

    /**
     * 负载均衡权重
     */
    @Schema(description = "负载均衡权重", example = "1")
    @ExcelProperty(value = "负载均衡权重")
    private Integer weight;

    /**
     * 重试间隔（单位：秒）
     */
    @Schema(description = "重试间隔（单位：秒）", example = "5")
    @ExcelProperty(value = "重试间隔（单位：秒）")
    private Integer retryInterval;

    /**
     * 重试次数
     */
    @Schema(description = "重试次数", example = "0")
    @ExcelProperty(value = "重试次数")
    private Integer maxRetries;

    /**
     * 发送上限
     */
    @Schema(description = "发送上限")
    @ExcelProperty(value = "发送上限")
    private Integer maximum;

    /**
     * 各个厂商独立配置
     */
    @Schema(description = "各个厂商独立配置")
    @ExcelProperty(value = "各个厂商独立配置")
    private String supplierConfig;

    /**
     * 是否为默认存储
     */
    @Schema(description = "是否为默认存储", example = "true")
    @ExcelProperty(value = "是否为默认存储")
    private Boolean isDefault;

    /**
     * 状态
     */
    @Schema(description = "状态", example = "1")
    @ExcelProperty(value = "状态", converter = ExcelBaseEnumConverter.class)
    private StatusEnum status;
}