import http from '#/utils/request';

const BASE_URL = '/dashboard';

/* ==================== API 定义 ==================== */
export const dashboardApi = {
  /** 查询公告列表 */
  listNotice: () => {
    return http.get<DashboardNoticeResp[]>(`${BASE_URL}/notice`);
  },
  /** 查询 PV 总览 */
  getOverviewPv: () => {
    return http.get<DashboardOverviewCommonResp>(
      `${BASE_URL}/analysis/overview/pv`,
    );
  },
  /** 查询 IP 总览 */
  getOverviewIp: () => {
    return http.get<DashboardOverviewCommonResp>(
      `${BASE_URL}/analysis/overview/ip`,
    );
  },
  /** 查询地域分析 */
  getAnalysisGeo: () => {
    return http.get<DashboardChartCommonResp[]>(`${BASE_URL}/analysis/geo`);
  },
  /** 查询访问趋势 */
  getAccessTrend: (days: number) => {
    return http.get<DashboardAccessTrendResp[]>(
      `${BASE_URL}/access/trend/${days}`,
    );
  },
  /** 查询访问时段分析 */
  getAnalysisTimeslot: () => {
    return http.get<DashboardChartCommonResp[]>(`${BASE_URL}/analysis/timeslot`);
  },
  /** 查询模块分析 */
  getAnalysisModule: () => {
    return http.get<DashboardChartCommonResp[]>(`${BASE_URL}/analysis/module`);
  },
  /** 查询终端分析 */
  getAnalysisOs: () => {
    return http.get<DashboardChartCommonResp[]>(`${BASE_URL}/analysis/os`);
  },
  /** 查询浏览器分析 */
  getAnalysisBrowser: () => {
    return http.get<DashboardChartCommonResp[]>(`${BASE_URL}/analysis/browser`);
  },
};

/* ==================== Schema 定义 ==================== */

/** 仪表盘公告类型 */
export interface DashboardNoticeResp {
  id: string;
  title: string;
  type: number;
  isTop: boolean;
}

/** 仪表盘访问趋势类型 */
export interface DashboardAccessTrendResp {
  date: string;
  pvCount: number;
  ipCount: number;
}

/** 仪表盘通用图表类型 */
export interface DashboardChartCommonResp {
  name: string;
  value: number;
}

/** 仪表盘通用总览类型 */
export interface DashboardOverviewCommonResp {
  total: number;
  today: number;
  growth: number;
  dataList: DashboardChartCommonResp[];
}
