
package top.wyhao.admin.generator.model.entity;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import lombok.Data;
import top.wyhao.starter.core.constant.StringConstants;

import java.io.Serial;
import java.util.List;
import java.util.Set;

/**
 * 内部生成配置信息

 * @since 2024/8/30 19:35
 */
@Data
public class InnerGenConfig extends GenConfig {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 字段配置信息
     */
    private List<GenFieldConfig> fieldConfigs;

    /**
     * 生成时间
     */
    private String datetime;

    /**
     * API 模块名称
     */
    private String apiModuleName;

    /**
     * API 名称
     */
    private String apiName;

    /**
     * 类名
     */
    private String className;

    /**
     * 类名前缀
     */
    private String classNamePrefix;

    /**
     * 子包名称
     */
    private String subPackageName;

    /**
     * 字典编码列表
     */
    private Set<String> dictCodes;

    /**
     * 是否包含必填字段
     */
    private boolean hasRequiredField;

    /**
     * 是否包含字典字段
     */
    private boolean hasDictField;

    /**
     * 是否包含 BigDecimal 字段
     */
    private boolean hasBigDecimalField;

    /**
     * 是否包含 Time 包字段
     */
    private boolean hasTimeField;

    /**
     * 引入的包
     */
    private List<String> imports;

    public InnerGenConfig() {
    }

    public InnerGenConfig(GenConfig genConfig) {
        BeanUtil.copyProperties(genConfig, this);
        this.setDatetime(DateUtil.date().toString("yyyy/MM/dd HH:mm"));
        this.setApiName(StrUtil.lowerFirst(this.getClassNamePrefix()));
    }

    @Override
    public void setPackageName(String packageName) {
        super.setPackageName(packageName);
        String realPackageName = this.getPackageName();
        this.setApiModuleName(StrUtil.subSuf(realPackageName, StrUtil
            .lastIndexOfIgnoreCase(realPackageName, StringConstants.DOT) + 1));
    }

    @Override
    public String getClassNamePrefix() {
        return super.getClassNamePrefix();
    }
}
