
package top.wyhao.admin.system.model.enums;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import top.wyhao.admin.system.entity.SysUser;
import top.wyhao.admin.system.model.vo.config.SecurityConfigVO;
import top.wyhao.admin.system.service.ConfigService;
import top.wyhao.admin.system.service.UserPasswordHistoryService;
import top.wyhao.starter.core.constant.GlobalConstants;
import top.wyhao.starter.core.constant.RegexConstants;
import top.wyhao.starter.core.util.validation.ValidationUtils;

import java.util.Map;

/**
 * 密码策略枚举
 *


 * @since 2024/5/9 11:25
 */
public enum PasswordPolicies {

    /**
     * 密码错误锁定阈值
     */
    PASSWORD_ERROR_LOCK_COUNT("密码错误锁定阈值取值范围为 %d-%d", GlobalConstants.Boolean.NO, 10, "连续 %s 次输入错误密码，账号被锁定 %s 分钟"),

    /**
     * 账号锁定时长（分钟）
     */
    PASSWORD_ERROR_LOCK_MINUTES("账号锁定时长取值范围为 %d-%d 分钟", 1, 1440, "您的账号已被锁定，预计解锁时间为 %s，请稍后再试"),

    /**
     * 密码有效期（天）
     */
    PASSWORD_EXPIRATION_DAYS("密码有效期取值范围为 %d-%d 天", GlobalConstants.Boolean.NO, 999, null),

    /**
     * 密码到期提醒（天）
     */
    PASSWORD_EXPIRATION_WARNING_DAYS("密码到期提醒取值范围为 %d-%d 天", GlobalConstants.Boolean.NO, 998, null) {
        @Override
        public void validateRange(int value, Map<String, String> policyMap) {
            if (CollUtil.isEmpty(policyMap)) {
                super.validateRange(value, policyMap);
                return;
            }
            SecurityConfigVO securityConfigVO = SpringUtil.getBean(ConfigService.class).getSecurityConfig();
            Integer passwordExpirationDays = ObjectUtil.defaultIfNull(Convert.toInt(policyMap
                .get(PASSWORD_EXPIRATION_DAYS.name())), securityConfigVO.getPasswordExpireDays());
            if (passwordExpirationDays > GlobalConstants.Boolean.NO) {
                ValidationUtils.throwIf(value >= passwordExpirationDays, "密码到期提醒时间应小于密码有效期");
                return;
            }
            super.validateRange(value, policyMap);
        }
    },

    /**
     * 密码最小长度
     */
    PASSWORD_MIN_LENGTH("密码最小长度取值范围为 %d-%d", 8, 32, "密码最小长度为 %d 个字符") {
        @Override
        public void validate(String password, int value, SysUser user) {
            // 最小长度校验
            ValidationUtils.throwIf(StrUtil.length(password) < value, this.getMsg().formatted(value));
            // 完整校验
            int passwordMaxLength = this.getMax();
            ValidationUtils.throwIf(!ReUtil.isMatch(RegexConstants.PASSWORD_TEMPLATE
                .formatted(value, passwordMaxLength), password), "密码长度为 {}-{} 个字符，支持大小写字母、数字、特殊字符，至少包含字母和数字", value, passwordMaxLength);
        }
    },

    /**
     * 密码是否必须包含特殊字符
     */
    PASSWORD_REQUIRE_SYMBOLS("密码是否必须包含特殊字符取值只能为是（%d）或否（%d）", GlobalConstants.Boolean.NO, GlobalConstants.Boolean.YES, "密码必须包含特殊字符") {
        @Override
        public void validateRange(int value, Map<String, String> policyMap) {
            ValidationUtils.throwIf(value != GlobalConstants.Boolean.YES && value != GlobalConstants.Boolean.NO, this
                .getDescription()
                .formatted(GlobalConstants.Boolean.YES, GlobalConstants.Boolean.NO));
        }

        @Override
        public void validate(String password, int value, SysUser user) {
            ValidationUtils.throwIf(value == GlobalConstants.Boolean.YES && !ReUtil
                .isMatch(RegexConstants.SPECIAL_CHARACTER, password), this.getMsg());
        }
    },

    /**
     * 密码是否允许包含用户名
     */
    PASSWORD_ALLOW_CONTAIN_USERNAME("密码是否允许包含用户名取值只能为是（%d）或否（%d）", GlobalConstants.Boolean.NO, GlobalConstants.Boolean.YES, "密码不允许包含正反序用户名") {
        @Override
        public void validateRange(int value, Map<String, String> policyMap) {
            ValidationUtils.throwIf(value != GlobalConstants.Boolean.YES && value != GlobalConstants.Boolean.NO, this
                .getDescription()
                .formatted(GlobalConstants.Boolean.YES, GlobalConstants.Boolean.NO));
        }

        @Override
        public void validate(String password, int value, SysUser user) {
            if (value <= GlobalConstants.Boolean.NO) {
                String username = user.getUsername();
                ValidationUtils.throwIf(CharSequenceUtil.containsAnyIgnoreCase(password, username, StrUtil
                    .reverse(username)), this.getMsg());
            }
        }
    },

    /**
     * 历史密码重复校验次数
     */
    PASSWORD_REPETITION_TIMES("历史密码重复校验次数取值范围为 %d-%d", 3, 32, "新密码不得与历史前 %d 次密码重复") {
        @Override
        public void validate(String password, int value, SysUser user) {
            UserPasswordHistoryService userPasswordHistoryService = SpringUtil
                .getBean(UserPasswordHistoryService.class);
            ValidationUtils.throwIf(userPasswordHistoryService.isPasswordReused(user.getId(), password, value), this
                .getMsg()
                .formatted(value));
        }
    },;

    /**
     * 描述
     */
    private final String description;

    /**
     * 最小值
     */
    private final Integer min;

    /**
     * 最大值
     */
    private final Integer max;

    /**
     * 提示信息
     */
    private final String msg;

    PasswordPolicies(String description, Integer min, Integer max, String msg) {
        this.description = description;
        this.min = min;
        this.max = max;
        this.msg = msg;
    }

    public String getDescription() {
        return description;
    }

    public Integer getMin() {
        return min;
    }

    public Integer getMax() {
        return max;
    }

    public String getMsg() {
        return msg;
    }

    /**
     * 策略类别
     */
    public static final ConfigCategory CATEGORY = ConfigCategory.PASSWORD;

    /**
     * 校验取值范围
     *
     * @param value     值
     * @param policyMap 策略集合
     */
    public void validateRange(int value, Map<String, String> policyMap) {
        Integer minValue = this.getMin();
        Integer maxValue = this.getMax();
        ValidationUtils.throwIf(value < minValue || value > maxValue, this.getDescription()
            .formatted(minValue, maxValue));
    }

    /**
     * 校验
     *
     * @param password 密码
     * @param value    策略值
     * @param user     用户信息
     */
    public void validate(String password, int value, SysUser user) {
        // 无需校验
    }
}
