package top.wyhao.system.admin.app.assembler;

import org.mapstruct.Mapper;
import top.wyhao.system.admin.app.dto.LoginLogExcelResult;
import top.wyhao.system.admin.app.dto.LoginLogResult;
import top.wyhao.system.admin.domain.model.LoginLog;
import top.wyhao.web.convert.MapStructConfig;

import java.util.List;

/**
 * 登录日志对象转换
 */
@Mapper(config = MapStructConfig.class)
public interface LoginLogAssembler {

    LoginLogResult toResult(LoginLog log);

    List<LoginLogResult> toResultList(List<LoginLog> logs);

    LoginLogExcelResult toExcel(LoginLog log);

    List<LoginLogExcelResult> toExcelList(List<LoginLog> logs);
}
