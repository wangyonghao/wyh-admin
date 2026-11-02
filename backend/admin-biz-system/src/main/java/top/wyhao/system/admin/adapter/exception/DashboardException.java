package top.wyhao.system.admin.adapter.exception;

import top.wyhao.cmn.core.exception.BizException;

/**
 * 仪表盘业务异常
 */
public class DashboardException extends BizException {

    public DashboardException(String message) {
        super(message);
    }

    public DashboardException(String code, String message) {
        super(code, message);
    }

    public static DashboardException of(String message) {
        return new DashboardException(message);
    }

    public static DashboardException of(String code, String message) {
        return new DashboardException(code, message);
    }

    public static DashboardException accessTrendDaysNotSupported() {
        return of("DASHBOARD_ACCESS_TREND_DAYS_NOT_SUPPORTED", "仅支持查询近 7/30 天访问趋势信息");
    }
}
