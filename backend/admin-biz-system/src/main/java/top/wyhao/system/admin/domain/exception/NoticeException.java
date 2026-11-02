package top.wyhao.system.admin.domain.exception;

import top.wyhao.cmn.core.exception.BizException;

/**
 * 公告业务异常
 */
public class NoticeException extends BizException {

    public NoticeException(String message) {
        super(message);
    }

    public NoticeException(String code, String message) {
        super(code, message);
    }

    public static NoticeException of(String message) {
        return new NoticeException(message);
    }

    public static NoticeException of(String code, String message) {
        return new NoticeException(code, message);
    }

    public static NoticeException notFound() {
        return of("NOTICE_NOT_FOUND", "公告不存在");
    }

    public static NoticeException createFailed() {
        return of("CREATE_FAILED", "创建失败");
    }

    public static NoticeException updateFailed() {
        return of("UPDATE_FAILED", "更新失败");
    }

    public static NoticeException deleteFailed() {
        return of("DELETE_FAILED", "删除失败");
    }

    public static NoticeException idRequired() {
        return of("REQUIRE_NONE_NULL", "ID 不能为空");
    }

    public static NoticeException notFoundOrNoAccess() {
        return of("NOTICE_NOT_FOUND_OR_NO_ACCESS", "公告不存在或无权限访问");
    }

    public static NoticeException messageNotFoundOrNoAccess() {
        return of("NOTICE_MESSAGE_NOT_FOUND_OR_NO_ACCESS", "消息不存在或无权限访问");
    }

    public static NoticeException publishedStatusUpdateNotAllowed() {
        return of("NOTICE_PUBLISHED_STATUS_UPDATE_NOT_ALLOWED", "公告已发布，不允许修改状态");
    }

    public static NoticeException publishedTimingUpdateNotAllowed() {
        return of("NOTICE_PUBLISHED_TIMING_UPDATE_NOT_ALLOWED", "公告已发布，不允许修改定时发布信息");
    }

    public static NoticeException publishedScopeUpdateNotAllowed() {
        return of("NOTICE_PUBLISHED_SCOPE_UPDATE_NOT_ALLOWED", "公告已发布，不允许修改通知范围");
    }

    public static NoticeException publishedUsersUpdateNotAllowed() {
        return of("NOTICE_PUBLISHED_USERS_UPDATE_NOT_ALLOWED", "公告已发布，不允许修改通知用户");
    }

    public static NoticeException publishedMethodsUpdateNotAllowed() {
        return of("NOTICE_PUBLISHED_METHODS_UPDATE_NOT_ALLOWED", "公告已发布，不允许修改通知方式");
    }
}
