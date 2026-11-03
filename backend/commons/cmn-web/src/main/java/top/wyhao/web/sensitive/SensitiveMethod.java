
package top.wyhao.web.sensitive;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.DesensitizedUtil;

import java.util.function.UnaryOperator;

/**
 * 脱敏类型
 */
public enum SensitiveMethod {

    /**
     * 手机号码
     * <p>保留前 3 位和后 4 位，例如：135****2210</p>
     */
    MOBILE_PHONE(DesensitizedUtil::mobilePhone),

    /**
     * 固定电话
     * <p>
     * 保留前 4 位和后 2 位
     * </p>
     */
    FIXED_PHONE(DesensitizedUtil::fixedPhone),

    /**
     * 电子邮箱
     *
     * <p>
     * 邮箱前缀仅保留第 1 个字母，@ 符号及后面的地址不脱敏，例如：d**@126.com
     * </p>
     */
    EMAIL(DesensitizedUtil::email),

    /**
     * 身份证号
     * <p>
     * 保留前 1 位和后 2 位
     * </p>
     */
    ID_CARD(s -> DesensitizedUtil.idCardNum(s, 3, 4)),

    /**
     * 银行卡
     * <p>
     * 由于银行卡号长度不定，所以只保留前 4 位，后面保留的位数根据卡号决定展示 1-4 位
     * <ul>
     * <li>1234 2222 3333 4444 6789 9 => 1234 **** **** **** **** 9</li>
     * <li>1234 2222 3333 4444 6789 91 => 1234 **** **** **** **** 91</li>
     * <li>1234 2222 3333 4444 678 => 1234 **** **** **** 678</li>
     * <li>1234 2222 3333 4444 6789 => 1234 **** **** **** 6789</li>
     * </ul>
     * </p>
     */
    BANK_CARD(DesensitizedUtil::bankCard),

    /**
     * 中国大陆车牌（包含普通车辆、新能源车辆）
     * <p>
     * 例如：苏D40000 => 苏D4***0
     * </p>
     */
    CAR_LICENSE(DesensitizedUtil::carLicense),

    /**
     * 中文名
     * <p>
     * 只保留第 1 个汉字，例如：李**
     * </p>
     */
    CHINESE_NAME(DesensitizedUtil::chineseName),

    /**
     * 地址
     * <p>
     * 只显示到地区，不显示详细地址，例如：北京市海淀区****
     * </p>
     */
    ADDRESS(s -> DesensitizedUtil.address(s, 8)),

    /**
     * 护照，例如：PJ1234567 → PJ*****67
     */
    PASSPORT(DesensitizedUtil::passport),
    /**
     * IPv4 地址
     * <p>
     * 例如：192.0.2.1 => 192.*.*.*
     */
    IPV4(DesensitizedUtil::ipv4),

    /**
     * IPv6 地址
     * <p>
     * 例如：2001:0db8:86a3:08d3:1319:8a2e:0370:7344 => 2001:*:*:*:*:*:*:*
     * </p>
     */
    IPV6(DesensitizedUtil::ipv6),

    /**
     * 密钥 Key
     */
    SECRET_KEY(s -> CharSequenceUtil.hide(s, 4, 4)),

    /**
     * 清空为""
     */
    CLEAR(s -> DesensitizedUtil.clear()),

    /**
     * 清空为null
     */
    CLEAR_TO_NULL(s -> DesensitizedUtil.clearToNull()),

    /**
     * 完全脱敏：所有字符都用*代替
     */
    FULL(DesensitizedUtil::password),
    /**
     * 不脱敏，保留原始值 (用于默认值或不需要脱敏的情况)
     */
    NONE(s -> s);

    private final UnaryOperator<String> masker;

    SensitiveMethod(UnaryOperator<String> masker) {
        this.masker = masker;
    }

    public String mask(String value) {
        return masker.apply(value);
    }
}
