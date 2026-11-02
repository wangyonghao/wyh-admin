package top.wyhao.system.admin.domain.model;

import top.wyhao.cmn.core.enums.BaseEnum;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OrgApplicationStatus implements BaseEnum {
    PENDING(0, "PENDING"),
    APPROVED(1, "APPROVED"),
    REJECTED(2, "REJECTED"),
    CANCELLED(3, "CANCELLED");

    @JsonValue
    private final Integer value;
    private final String description;
}
