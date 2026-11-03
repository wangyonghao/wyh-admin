package top.wyhao.starter.quartz.spi;

/**
 * 已注册任务描述，供管理端下拉选择。
 */
public class JobHandlerDescriptor {

    private final String code;
    private final String name;
    private final String description;
    private final boolean allowConcurrent;

    public JobHandlerDescriptor(String code, String name, String description, boolean allowConcurrent) {
        this.code = code;
        this.name = name;
        this.description = description;
        this.allowConcurrent = allowConcurrent;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public boolean isAllowConcurrent() {
        return allowConcurrent;
    }
}
