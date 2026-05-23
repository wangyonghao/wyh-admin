
package top.wyhao.admin.config.doc;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.annotation.SaMode;
import cn.hutool.core.text.CharSequenceUtil;
import io.swagger.v3.oas.models.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.customizers.GlobalOperationCustomizer;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

/**
 * 全局描述定制器 - 处理 sa-token 的注解权限码
 *

 * @since 2025/1/24 14:59
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OperationCustomizer implements GlobalOperationCustomizer {

    @Override
    public Operation customize(Operation operation, HandlerMethod handlerMethod) {
        // 将 sa-token 注解数据添加到 operation 的描述中
        // 权限
        List<String> noteList = this.getPermissionDescription(handlerMethod);

        // 如果注解数据列表为空，直接返回原 operation
        if (noteList.isEmpty()) {
            return operation;
        }
        // 拼接注解数据为字符串
        String noteStr = CharSequenceUtil.join("<br/>", noteList);
        // 获取原描述
        String originalDescription = operation.getDescription();
        // 根据原描述是否为空，更新描述
        String newDescription = CharSequenceUtil.isNotEmpty(originalDescription)
            ? originalDescription + "<br/>" + noteStr
            : noteStr;

        // 设置新描述
        operation.setDescription(newDescription);
        return operation;
    }


    /**
     * 获取 sa-token 注解信息
     *
     * @param handlerMethod 处理程序方法
     * @return 包含权限和角色校验信息的列表
     */
    private List<String> getPermissionDescription(HandlerMethod handlerMethod) {
        List<String> values = new ArrayList<>();

        // 获取权限校验信息
        String permissionInfo = getAnnotationInfo(handlerMethod, SaCheckPermission.class, "权限校验：");
        if (!permissionInfo.isEmpty()) {
            values.add(permissionInfo);
        }

        // 获取角色校验信息
        String roleInfo = getAnnotationInfo(handlerMethod, SaCheckRole.class, "角色校验：");
        if (!roleInfo.isEmpty()) {
            values.add(roleInfo);
        }

        return values;
    }

    /**
     * 获取类和方法上指定注解的信息
     *
     * @param handlerMethod   处理程序方法
     * @param annotationClass 注解类
     * @param title           信息标题
     * @param <A>             注解类型
     * @return 拼接好的注解信息字符串
     */
    @SuppressWarnings("unchecked")
    private <A extends Annotation> String getAnnotationInfo(HandlerMethod handlerMethod,
                                                            Class<A> annotationClass,
                                                            String title) {
        StringBuilder infoBuilder = new StringBuilder();

        // 获取类上的注解
        A classAnnotation = handlerMethod.getBeanType().getAnnotation(annotationClass);
        if (classAnnotation != null) {
            appendAnnotationInfo(infoBuilder, "类：", classAnnotation);
        }

        // 获取方法上的注解
        A methodAnnotation = handlerMethod.getMethodAnnotation(annotationClass);
        if (methodAnnotation != null) {
            appendAnnotationInfo(infoBuilder, "方法：", methodAnnotation);
        }

        // 如果有注解信息，添加标题
        if (!infoBuilder.isEmpty()) {
            infoBuilder.insert(0, "<font style=\"color:red\" class=\"light-red\">" + title + "</font></br>");
        }

        return infoBuilder.toString();
    }

    /**
     * 拼接注解信息到 StringBuilder 中
     *
     * @param builder    用于拼接信息的 StringBuilder
     * @param prefix     前缀信息，如 "类：" 或 "方法："
     * @param annotation 注解对象
     */
    private void appendAnnotationInfo(StringBuilder builder, String prefix, Annotation annotation) {
        String[] values = null;
        SaMode mode = null;
        String type = "";
        String[] orRole = new String[0];

        if (annotation instanceof SaCheckPermission checkPermission) {
            values = checkPermission.value();
            mode = checkPermission.mode();
            type = checkPermission.type();
            orRole = checkPermission.orRole();
        } else if (annotation instanceof SaCheckRole checkRole) {
            values = checkRole.value();
            mode = checkRole.mode();
            type = checkRole.type();
        }

        if (values != null && mode != null) {
            builder.append("<font style=\"color:red\" class=\"light-red\">");
            builder.append(prefix);
            if (!type.isEmpty()) {
                builder.append("（类型：").append(type).append("）");
            }
            builder.append(getAnnotationNote(values, mode));
            if (orRole.length > 0) {
                builder.append(" 或 角色校验（").append(getAnnotationNote(orRole, mode)).append("）");
            }
            builder.append("</font></br>");
        }
    }

    /**
     * 根据注解的模式拼接注解值
     *
     * @param values 注解的值数组
     * @param mode   注解的模式（AND 或 OR）
     * @return 拼接好的注解值字符串
     */
    private String getAnnotationNote(String[] values, SaMode mode) {
        if (mode.equals(SaMode.AND)) {
            return String.join(" 且 ", values);
        } else {
            return String.join(" 或 ", values);
        }
    }
}
