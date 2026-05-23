
package top.wyhao.admin.system.service;

import top.wyhao.admin.system.model.vo.dashboard.DashboardAccessTrendResp;
import top.wyhao.admin.system.model.vo.dashboard.DashboardChartCommonResp;
import top.wyhao.admin.system.model.vo.dashboard.DashboardNoticeResp;
import top.wyhao.admin.system.model.vo.dashboard.DashboardOverviewCommonResp;

import java.io.IOException;
import java.util.List;

/**
 * 仪表盘业务接口
 *

 * @since 2023/9/8 21:32
 */
public interface DashboardService {

    /**
     * 查询公告列表
     *
     * @return 公告列表
     */
    List<DashboardNoticeResp> listNotice();

    /**
     * 查询 PV 总览
     *
     * @return PV 总览
     */
    DashboardOverviewCommonResp getOverviewPv();

    /**
     * 查询 IP 总览
     *
     * @return IP 总览
     */
    DashboardOverviewCommonResp getOverviewIp();

    /**
     * 查询地域分析信息
     *
     * @return 地域分析信息
     * @throws IOException /
     */
    List<DashboardChartCommonResp> getAnalysisGeo() throws IOException;

    /**
     * 查询访问趋势信息
     *
     * @param days 日期数
     * @return 访问趋势信息
     */
    List<DashboardAccessTrendResp> listAccessTrend(Integer days);

    /**
     * 查询访问时段分析信息
     *
     * @return 访问时段分析信息
     */
    List<DashboardChartCommonResp> getAnalysisTimeslot();

    /**
     * 查询模块分析信息
     *
     * @return 模块分析信息
     */
    List<DashboardChartCommonResp> getAnalysisModule();

    /**
     * 查询终端分析信息
     *
     * @return 终端分析信息
     */
    List<DashboardChartCommonResp> getAnalysisOs();

    /**
     * 查询浏览器分析信息
     *
     * @return 浏览器分析信息
     */
    List<DashboardChartCommonResp> getAnalysisBrowser();
}
