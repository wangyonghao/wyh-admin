
package top.wyhao.admin.system.model.vo.user;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import top.wyhao.common.security.util.LoginUtil;
import top.wyhao.starter.core.enums.GenderEnum;
import top.wyhao.starter.core.enums.StatusEnum;
import top.wyhao.starter.web.sensitive.annotation.Sensitive;
import top.wyhao.starter.web.sensitive.enums.SensitiveMethod;

import java.util.List;
import java.util.Objects;

/**
 * 用户响应参数
 *

 * @since 2023/2/20 21:08
 */
@Data
@Schema(description = "用户响应参数")
public class UserResult {
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
     * 用户名
     */
    @Schema(description = "用户名", example = "zhangsan")
    private String username;

    /**
     * 昵称
     */
    @Schema(description = "昵称", example = "张三")
    private String nickname;

    /**
     * 性别
     */
    @Schema(description = "性别", example = "1")
    private GenderEnum gender;

    /**
     * 头像地址
     */
    @Schema(description = "头像地址", example = "https://himg.bdimg.com/sys/portrait/item/public.1.81ac9a9e.rf1ix17UfughLQjNo7XQ_w.jpg")
    private String avatar;

    /**
     * 邮箱
     */
    @Schema(description = "邮箱", example = "c*******@126.com")
    @Sensitive(SensitiveMethod.EMAIL)
    private String email;

    /**
     * 手机号码
     */
    @Schema(description = "手机号码", example = "188****8888")
    @Sensitive(SensitiveMethod.MOBILE_PHONE)
    private String phone;

    /**
     * 状态
     */
    @Schema(description = "状态", example = "1")
    private StatusEnum status;

    /**
     * 是否为系统内置数据
     */
    @Schema(description = "是否为系统内置数据", example = "false")
    private Boolean isBuiltin;

    /**
     * 描述
     */
    @Schema(description = "描述", example = "张三描述信息")
    private String description;

    /**
     * 部门 ID
     */
    @Schema(description = "部门 ID", example = "5")
    private Long deptId;

    /**
     * 所属部门
     */
    @Schema(description = "所属部门", example = "测试部")
    private String deptName;

    /**
     * 角色名称列表
     */
    @Schema(description = "角色名称列表", example = "测试人员")
    private List<String> roleNames;

    public Boolean getDisabled() {
        return this.getIsBuiltin() || Objects.equals(this.getId(), LoginUtil.getUserId());
    }
}