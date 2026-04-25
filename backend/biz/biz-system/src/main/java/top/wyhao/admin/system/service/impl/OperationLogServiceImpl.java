/*
 * Copyright (c) 2022-present wangyonghao Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package top.wyhao.admin.system.service.impl;

import cn.crane4j.annotation.AutoOperate;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import top.wyhao.starter.core.enums.StatusEnum;
import top.wyhao.admin.system.mapper.OperationLogMapper;
import top.wyhao.admin.system.model.entity.SysOperationLog;
import top.wyhao.admin.system.model.query.LogQuery;
import top.wyhao.admin.system.model.vo.log.LogDetailResp;
import top.wyhao.admin.system.model.vo.log.LogResp;
import top.wyhao.admin.system.model.vo.log.LoginLogExportResp;
import top.wyhao.admin.system.model.vo.log.OperationLogExportResp;
import top.wyhao.admin.system.service.OperationLogService;
import top.wyhao.starter.core.model.LoginUser;
import top.wyhao.starter.core.util.IpUtils;
import top.wyhao.starter.core.util.validation.BizAssert;
import top.wyhao.starter.data.util.QueryWrapperUtil;
import top.wyhao.starter.web.ServletUtils;
import top.wyhao.starter.web.core.model.PageQuery;
import top.wyhao.starter.web.core.model.SortQuery;
import top.wyhao.starter.web.core.model.PageResult;
import top.wyhao.starter.excel.util.ExcelUtils;
import top.wyhao.starter.web.log.OperationLog;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 操作日志服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OperationLogServiceImpl implements OperationLogService {

    private final OperationLogMapper operationLogMapper;

    /**
     * 记录操作日志
     *
     * @param operationLog 操作日志事件
     */
    @Async
    @EventListener
    @Override
    public void log(OperationLog operationLog) {
        SysOperationLog operLog = BeanUtil.toBean(operationLog, SysOperationLog.class);
        // 远程查询操作地点
        operLog.setOperatorLocation(IpUtils.getIpv4Address(operationLog.getOperatorIp()));
        operationLogMapper.insert(operLog);
    }

    @Override
    public void recordLoginLog(LoginUser loginUser) {
        ServletUtils.RequestMeta requestMeta = ServletUtils.getRequestMeta();
        // 记录登录日志
        SysOperationLog operationLog = new SysOperationLog();
        operationLog.setOperation("login");
        operationLog.setObjectType("user");
        operationLog.setObjectId(loginUser.getUserId());
        operationLog.setOperatorId(loginUser.getUserId());
        operationLog.setOperatorName(loginUser.getUsername());
        operationLog.setOperatorIp(requestMeta.ip());
        operationLog.setOperatorLocation(requestMeta.address());
        operationLog.setOperateTime(LocalDateTime.now());
        operationLog.setRemark(requestMeta.address() + ", " + requestMeta.browser() + ", " + requestMeta.os()); // 登录设备信息
        operationLogMapper.insert(operationLog);
    }

    @Override
    public PageResult<LogResp> page(LogQuery query, PageQuery pageQuery) {
        QueryWrapper<SysOperationLog> queryWrapper = this.buildQueryWrapper(query);

        QueryWrapperUtil.applySort(queryWrapper, pageQuery.getSort(), SysOperationLog.class);
        return PageResult.build(operationLogMapper.selectLogPage(new Page<>(pageQuery.getPage(), pageQuery.getSize()), queryWrapper), LogResp.class);
    }

    @Override
    @AutoOperate(type = LogDetailResp.class)
    public LogDetailResp get(Long id) {
        SysOperationLog sysOperationLog = operationLogMapper.selectById(id);
        BizAssert.throwIfNotExists(sysOperationLog, "LogDO", "ID", id);
        return BeanUtil.copyProperties(sysOperationLog, LogDetailResp.class);
    }

    @Override
    public void exportLoginLog(LogQuery query, SortQuery sortQuery, HttpServletResponse response) {
        List<LoginLogExportResp> list = BeanUtil.copyToList(this.list(query, sortQuery), LoginLogExportResp.class);
        ExcelUtils.export(list, "导出登录日志数据", LoginLogExportResp.class, response);
    }

    @Override
    public void exportOperationLog(LogQuery query, SortQuery sortQuery, HttpServletResponse response) {
        List<OperationLogExportResp> list = BeanUtil.copyToList(this
                .list(query, sortQuery), OperationLogExportResp.class);
        ExcelUtils.export(list, "导出操作日志数据", OperationLogExportResp.class, response);
    }

    /**
     * 查询列表
     *
     * @param query     查询条件
     * @param sortQuery 排序查询条件
     * @return 列表信息
     */
    private List<LogResp> list(LogQuery query, SortQuery sortQuery) {
        QueryWrapper<SysOperationLog> queryWrapper = this.buildQueryWrapper(query);
        QueryWrapperUtil.applySort(queryWrapper, sortQuery.getSort(), SysOperationLog.class);
        return operationLogMapper.selectLogList(queryWrapper);
    }

    /**
     * 构建 QueryWrapper
     *
     * @param query 查询条件
     * @return QueryWrapper
     */
    private QueryWrapper<SysOperationLog> buildQueryWrapper(LogQuery query) {
        String description = query.getDescription();
        String module = query.getModule();
        String ip = query.getIp();
        String createUserString = query.getCreateUserString();
        StatusEnum status = query.getStatus();
        List<LocalDateTime> createTimeList = query.getCreateTime();
        return new QueryWrapper<SysOperationLog>().and(StrUtil.isNotBlank(description), q -> q.like("t1.description", description)
                        .or()
                        .like("t1.module", description))
                .eq(StrUtil.isNotBlank(module), "t1.module", module)
                .and(StrUtil.isNotBlank(ip), q -> q.like("t1.ip", ip).or().like("t1.address", ip))
                .and(StrUtil.isNotBlank(createUserString), q -> q.like("t2.username", createUserString)
                        .or()
                        .like("t2.nickname", createUserString))
                .eq(status != null, "t1.status", status)
                .between(CollUtil.isNotEmpty(createTimeList), "t1.create_time", CollUtil.getFirst(createTimeList), CollUtil
                        .getLast(createTimeList));
    }
}
