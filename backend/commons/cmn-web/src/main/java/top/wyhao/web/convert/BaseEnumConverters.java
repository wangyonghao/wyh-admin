package top.wyhao.web.convert;

import org.springframework.stereotype.Component;
import top.wyhao.cmn.core.enums.BaseEnum;
import top.wyhao.cmn.core.enums.GenderEnum;
import top.wyhao.cmn.core.enums.ResultStatusEnum;
import top.wyhao.cmn.core.enums.StatusEnum;

/**
 * BaseEnum 与存储值之间的 MapStruct 转换
 */
@Component
public class BaseEnumConverters {

    public Integer toValue(BaseEnum source) {
        return source == null ? null : source.getValue();
    }

    public String toValueString(BaseEnum source) {
        return source == null ? null : String.valueOf(source.getValue());
    }

    public StatusEnum toStatusEnum(Integer value) {
        return BaseEnum.getByValue(value, StatusEnum.class);
    }

    public StatusEnum toStatusEnum(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return BaseEnum.getByValue(Integer.valueOf(value), StatusEnum.class);
        } catch (NumberFormatException ignored) {
            try {
                return StatusEnum.valueOf(value);
            } catch (IllegalArgumentException ex) {
                return null;
            }
        }
    }

    public GenderEnum toGenderEnum(Integer value) {
        return BaseEnum.getByValue(value, GenderEnum.class);
    }

    public ResultStatusEnum toResultStatusEnum(Integer value) {
        return BaseEnum.getByValue(value, ResultStatusEnum.class);
    }
}
