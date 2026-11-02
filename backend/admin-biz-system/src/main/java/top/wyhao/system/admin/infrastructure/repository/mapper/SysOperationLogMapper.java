package top.wyhao.system.admin.infrastructure.repository.mapper;

import com.alicp.jetcache.anno.Cached;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import top.wyhao.system.admin.infrastructure.persistence.dataobject.SysOperationLogDO;
import top.wyhao.system.admin.app.dto.DashboardAccessTrendResp;
import top.wyhao.system.admin.app.dto.DashboardChartCommonResp;
import top.wyhao.system.admin.app.dto.DashboardOverviewCommonResp;
import top.wyhao.mybatis.model.BaseMapper;
import top.wyhao.cmn.core.constant.CacheConstants;

import java.util.Date;
import java.util.List;
import top.wyhao.system.admin.app.dto.OperationLogExcelResult;
import top.wyhao.system.admin.app.dto.OperationLogResult;

/**
 * 系统日志 Mapper
 *

 * @since 2022/12/22 21:47
 */
@Mapper
public interface SysOperationLogMapper extends BaseMapper<SysOperationLogDO> {

    /**
     * 分页查询列表
     *
     * @param page         分页条件
     * @param queryWrapper 查询条件
     * @return 分页列表信息
     */
    IPage<OperationLogResult> selectLogPage(@Param("page") IPage<SysOperationLogDO> page,
                                           @Param(Constants.WRAPPER) QueryWrapper<SysOperationLogDO> queryWrapper);

    /**
     * 查询列表
     *
     * @param queryWrapper 查询条件
     * @return 列表信息
     */
    List<OperationLogExcelResult> selectLogList(@Param(Constants.WRAPPER) Wrapper<SysOperationLogDO> queryWrapper);

    /**
     * 查询仪表盘 PV 总览
     *
     * @return 仪表盘 PV 总览
     */
    DashboardOverviewCommonResp selectDashboardOverviewPv();

    /**
     * 查询仪表盘 IP 总览
     *
     * @return 仪表盘 IP 总览
     */
    DashboardOverviewCommonResp selectDashboardOverviewIp();

    /**
     * 查询仪表盘 PV 近 N 月各月份信息
     *
     * @param months 近 N 月月份列表
     * @return 仪表盘 PV 近 N 月各月份信息
     */
    @Cached(key = "#months[0]", name = CacheConstants.DASHBOARD_KEY_PREFIX + "PV:")
    List<DashboardChartCommonResp> selectListDashboardAnalysisPv(@Param("months") List<String> months);

    /**
     * 查询仪表盘 IP 近 N 月各月份信息
     *
     * @param months 近 N 月月份列表
     * @return 仪表盘 IP 近 N 月各月份信息
     */
    @Cached(key = "#months[0]", name = CacheConstants.DASHBOARD_KEY_PREFIX + "IP:")
    List<DashboardChartCommonResp> selectListDashboardAnalysisIp(@Param("months") List<String> months);

    /**
     * 查询仪表盘地域分析信息
     *
     * @return 仪表盘地域分析信息
     */
    List<DashboardChartCommonResp> selectListDashboardAnalysisGeo();

    /**
     * 查询仪表盘访问趋势信息
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 仪表盘访问趋势信息
     */
    List<DashboardAccessTrendResp> selectListDashboardAccessTrend(@Param("startTime") Date startTime,
                                                                  @Param("endTime") Date endTime);

    /**
     * 查询仪表盘访问时段分析信息
     *
     * @return 仪表盘访问时段分析信息
     */
    List<DashboardChartCommonResp> selectListDashboardAnalysisTimeslot();

    /**
     * 查询仪表盘模块分析信息
     *
     * @param top 显示数量
     * @return 仪表盘模块分析信息
     */
    List<DashboardChartCommonResp> selectListDashboardAnalysisModule(@Param("top") Integer top);

    /**
     * 查询仪表盘终端分析信息
     *
     * @param top 显示数量
     * @return 仪表盘终端分析信息
     */
    List<DashboardChartCommonResp> selectListDashboardAnalysisOs(@Param("top") Integer top);

    /**
     * 查询仪表盘浏览器分析信息
     *
     * @param top 显示数量
     * @return 仪表盘浏览器分析信息
     */
    List<DashboardChartCommonResp> selectListDashboardAnalysisBrowser(@Param("top") Integer top);
}
