package top.wyhao.system.admin.domain.model;

import top.wyhao.cmn.core.enums.BaseEnum;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 成员关系状态。
 */
@Getter
@RequiredArgsConstructor
public enum MembershipStatus implements BaseEnum {

    DISABLED(0, "DISABLED"),
    NORMAL(1, "NORMAL"),
    INVITED(2, "INVITED");

    @JsonValue
    private final Integer value;
    private final String description;
}
