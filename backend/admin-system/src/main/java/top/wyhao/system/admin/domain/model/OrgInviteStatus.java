package top.wyhao.system.admin.domain.model;

import top.wyhao.cmn.core.enums.BaseEnum;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OrgInviteStatus implements BaseEnum {
    PENDING(0, "PENDING"),
    ACCEPTED(1, "ACCEPTED"),
    REJECTED(2, "REJECTED"),
    EXPIRED(3, "EXPIRED"),
    REVOKED(4, "REVOKED");

    @JsonValue
    private final Integer value;
    private final String description;
}
