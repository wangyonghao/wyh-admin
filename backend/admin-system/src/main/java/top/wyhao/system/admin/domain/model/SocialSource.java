
package top.wyhao.system.admin.domain.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 第三方账号平台枚举
 *

 * @since 2023/10/19 21:22
 */
@Getter
@RequiredArgsConstructor
public enum SocialSource {

    /**
     * 码云
     */
    GITEE("码云"),

    /**
     * GitHub
     */
    GITHUB("GitHub"),;

    private final String description;
}
