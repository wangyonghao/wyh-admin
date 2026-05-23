
package top.wyhao.admin.system.model.enums;

import cn.hutool.core.collection.CollUtil;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import top.wyhao.starter.core.enums.BaseEnum;

import java.util.List;

/**
 * 数据导入策略
 *

 * @since 2024/6/17 18:33
 */
@Getter
@RequiredArgsConstructor
public enum ImportPolicies implements BaseEnum {

    /**
     * 跳过该行
     */
    SKIP(1, "跳过该行"),

    /**
     * 修改数据
     */
    UPDATE(2, "修改数据"),

    /**
     * 停止导入
     */
    EXIT(3, "停止导入");

    private final Integer value;
    private final String description;

    public boolean validate(ImportPolicies importPolicy, String data, List<String> existList) {
        return this == importPolicy && CollUtil.isNotEmpty(existList) && existList.contains(data);
    }
}
