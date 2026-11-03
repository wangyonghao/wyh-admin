package top.wyhao.system.admin.app.assembler;

import org.mapstruct.Mapper;
import top.wyhao.system.admin.domain.model.SmsLog;
import top.wyhao.web.convert.MapStructConfig;

import java.util.List;
import top.wyhao.system.admin.app.dto.SmsLogRequest;
import top.wyhao.system.admin.app.dto.SmsLogResult;

/**
 * 短信日志对象转换
 */
@Mapper(config = MapStructConfig.class)
public interface SmsLogAssembler {

    SmsLogResult toResult(SmsLog log);

    List<SmsLogResult> toResultList(List<SmsLog> logs);

    SmsLog toEntity(SmsLogRequest request);
}
