package top.wyhao.system.admin.app.assembler;

import org.mapstruct.Mapper;
import top.wyhao.system.admin.domain.model.OperationLog;
import top.wyhao.web.convert.MapStructConfig;

import java.util.List;
import top.wyhao.system.admin.app.dto.OperationLogDetailResult;
import top.wyhao.system.admin.app.dto.OperationLogExcelResult;
import top.wyhao.system.admin.app.dto.OperationLogResult;

/**
 * 操作日志对象转换
 */
@Mapper(config = MapStructConfig.class)
public interface OperationLogAssembler {

    OperationLog toEntity(top.wyhao.web.log.OperationLog operationLog);

    OperationLogResult toResult(OperationLog log);

    List<OperationLogResult> toResultList(List<OperationLog> logs);

    OperationLogDetailResult toDetail(OperationLog log);

    OperationLogExcelResult toExcel(OperationLogExcelResult source);

    List<OperationLogExcelResult> toExcelList(List<OperationLogExcelResult> sources);
}
