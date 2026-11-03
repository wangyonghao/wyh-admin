package top.wyhao.system.admin.domain.exception;

import cn.hutool.core.util.StrUtil;
import top.wyhao.cmn.core.exception.BizException;

/**
 * 组织（租户成员）业务异常。
 */
public class OrgException extends BizException {

    public OrgException(String code, String message) {
        super(code, message);
    }

    public static OrgException of(String code, String message) {
        return new OrgException(code, message);
    }

    public static OrgException notMember() {
        return of("ORG_NOT_MEMBER", "你不是该组织的成员");
    }

    public static OrgException alreadyMember() {
        return of("ORG_ALREADY_MEMBER", "你已加入该组织");
    }

    public static OrgException notFound() {
        return of("ORG_NOT_FOUND", "组织不存在");
    }

    public static OrgException packageMissing() {
        return of("ORG_PACKAGE_MISSING", "系统尚未配置可用套餐，无法创建组织");
    }

    public static OrgException inviteNotFound() {
        return of("ORG_INVITE_NOT_FOUND", "邀请不存在或已失效");
    }

    public static OrgException inviteExpired() {
        return of("ORG_INVITE_EXPIRED", "邀请已过期");
    }

    public static OrgException inviteHandled() {
        return of("ORG_INVITE_HANDLED", "邀请已处理");
    }

    public static OrgException inviteEmailMismatch() {
        return of("ORG_INVITE_EMAIL_MISMATCH", "当前账号邮箱与邀请邮箱不一致");
    }

    public static OrgException applicationPending() {
        return of("ORG_APPLICATION_PENDING", "已提交申请，请等待管理员审核");
    }

    public static OrgException applicationNotFound() {
        return of("ORG_APPLICATION_NOT_FOUND", "申请不存在");
    }

    public static OrgException applicationHandled() {
        return of("ORG_APPLICATION_HANDLED", "申请已处理");
    }

    public static OrgException forbidden() {
        return of("ORG_FORBIDDEN", "无权执行该操作");
    }

    public static OrgException codeInvalid(String code) {
        return of("ORG_CODE_INVALID", StrUtil.format("组织编码 [{}] 无效", code));
    }

    public static OrgException emailRequired() {
        return of("ORG_EMAIL_REQUIRED", "请先绑定邮箱后再接受邀请");
    }
}
