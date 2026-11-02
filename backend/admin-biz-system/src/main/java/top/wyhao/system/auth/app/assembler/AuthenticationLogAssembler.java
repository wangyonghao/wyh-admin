package top.wyhao.system.auth.app.assembler;

import org.mapstruct.Mapper;
import top.wyhao.system.auth.app.dto.AuthenticationLogExcelResult;
import top.wyhao.system.auth.app.dto.AuthenticationLogResult;
import top.wyhao.system.auth.domain.authlog.AuthLog;
import top.wyhao.web.convert.MapStructConfig;

import java.util.List;

/**
 * 登录日志对象转换
 */
@Mapper(config = MapStructConfig.class)
public interface AuthenticationLogAssembler {

    AuthenticationLogResult toResult(AuthLog log);

    List<AuthenticationLogResult> toResultList(List<AuthLog> logs);

    AuthenticationLogExcelResult toExcel(AuthLog log);

    List<AuthenticationLogExcelResult> toExcelList(List<AuthLog> logs);
}
