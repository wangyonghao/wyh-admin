
package top.wyhao.system.admin.app.service.impl;

import top.wyhao.system.admin.infrastructure.persistence.AdminDoMapper;

import top.wyhao.system.admin.infrastructure.persistence.dataobject.SysOperationLogDO;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import top.wyhao.system.admin.app.assembler.OperationLogAssembler;
import top.wyhao.system.admin.domain.model.OperationLog;
import top.wyhao.system.admin.infrastructure.repository.mapper.SysOperationLogMapper;
import top.wyhao.system.admin.app.service.OperationLogService;
import top.wyhao.mybatis.query.QueryWrapperBuilder;
import top.wyhao.system.admin.domain.exception.OperationLogException;
import top.wyhao.starter.excel.util.ExcelUtils;
import top.wyhao.web.core.model.PageQuery;
import top.wyhao.mybatis.query.PageResult;

import java.util.List;
import top.wyhao.system.admin.app.dto.OperationLogDetailResult;
import top.wyhao.system.admin.app.dto.OperationLogExcelResult;
import top.wyhao.system.admin.app.dto.OperationLogQuery;
import top.wyhao.system.admin.app.dto.OperationLogResult;

/**
 * 操作日志服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OperationLogServiceImpl implements OperationLogService {

    private final SysOperationLogMapper operationLogMapper;
    private final OperationLogAssembler operationLogAssembler;

    /**
     * 异步记录操作日志
     *
     * @param operationLog 操作日志事件
     */
    @Async
    @EventListener
    @Override
    public void create(top.wyhao.web.log.OperationLog operationLog) {
        OperationLog operLog = operationLogAssembler.toEntity(operationLog);
        SysOperationLogDO __logDo = AdminDoMapper.toDo(operLog, SysOperationLogDO.class);
        operationLogMapper.insert(__logDo);
        AdminDoMapper.copyBack(__logDo, operLog);
    }


    @Override
    public PageResult<OperationLogResult> page(OperationLogQuery query, PageQuery pageQuery) {
        IPage<SysOperationLogDO> page = operationLogMapper.selectPage(new Page<>(pageQuery.getPage(), pageQuery.getPageSize()),
                QueryWrapperBuilder.build(query,SysOperationLogDO.class));
        return PageResult.of(page).map(item -> operationLogAssembler.toResult(AdminDoMapper.toDomain(item, OperationLog.class)));
    }

    @Override
    public OperationLogDetailResult detail(Long id) {
        OperationLog sysOperationLog = this.require(id);
        return operationLogAssembler.toDetail(sysOperationLog);
    }

    @Override
    public void export(OperationLogQuery query, HttpServletResponse response) {
        List<OperationLogExcelResult> list = operationLogAssembler.toExcelList(this.list(query));
        ExcelUtils.export(list, "导出操作日志数据", OperationLogExcelResult.class, response);
    }

    /**
     * 查询列表
     *
     * @param query 查询条件
     * @return 列表信息
     */
    private List<OperationLogExcelResult> list(OperationLogQuery query) {
        return operationLogMapper.selectLogList(QueryWrapperBuilder.build(query, SysOperationLogDO.class));
    }

    private OperationLog require(Long id) {
        OperationLog operationLog = AdminDoMapper.toDomain(operationLogMapper.selectById(id), OperationLog.class);
        if (operationLog == null) {
            throw OperationLogException.notFound();
        }
        return operationLog;
    }
}
