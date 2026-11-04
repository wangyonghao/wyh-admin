package top.wyhao.system.admin.domain.exception;

import cn.hutool.core.util.StrUtil;
import top.wyhao.cmn.core.exception.BizException;

/**
 * 用户业务异常
 */
public class UserException extends BizException {

    public UserException(String message) {
        super(message);
    }

    public UserException(String code, String message) {
        super(code, message);
    }

    public static UserException of(String message) {
        return new UserException(message);
    }

    public static UserException of(String code, String message) {
        return new UserException(code, message);
    }

    public static UserException notFound() {
        return of("USER_NOT_FOUND", "用户不存在");
    }

    public static UserException updateNotAllowed(String reason) {
        return of("USER_UPDATE_NOT_ALLOWED", reason);
    }

    public static UserException disableNotAllowed() {
        return updateNotAllowed("系统内置用户不允许禁用");
    }

    public static UserException roleChangeNotAllowed() {
        return updateNotAllowed("系统内置用户不允许变更角色");
    }

    public static UserException usernameExists() {
        return of("USERNAME_EXISTS", "用户名已被占用");
    }

    public static UserException emailExists() {
        return of("EMAIL_EXISTS", "邮箱已被占用");
    }

    public static UserException phoneExists() {
        return of("PHONE_EXISTS", "手机号已被占用");
    }

    public static UserException importFormatError() {
        return of("IMPORT_FORMAT_ERROR", "数据文件解析异常");
    }

    public static UserException importExpired() {
        return of("IMPORTATION_EXPIRED", "导入已过期，请重新上传");
    }

    public static UserException avatarSizeExceeded(long maxSizeMb) {
        return of("AVATAR_SIZE_EXCEEDED", StrUtil.format("头像大小不能超过 {} MB", maxSizeMb));
    }

    public static UserException downloadTemplateFailed(String detail) {
        return of("USER_IMPORT_TEMPLATE_DOWNLOAD_FAILED", "下载用户导入模板失败：" + detail);
    }

    public static UserException notFound(String ids) {
        return of("USER_NOT_FOUND", StrUtil.format("所选用户 [{}] 不存在", ids));
    }

    public static UserException passwordDecryptFailed() {
        return of("USER_PASSWORD_DECRYPT_FAILED", "密码解密失败");
    }

    public static UserException passwordFormatInvalid() {
        return of("USER_PASSWORD_FORMAT_INVALID", "密码长度为 8-32 个字符，支持大小写字母、数字、特殊字符，至少包含字母和数字");
    }

    public static UserException passwordFormatInvalid(int minLength, int maxLength) {
        return of("USER_PASSWORD_FORMAT_INVALID", StrUtil.format("密码长度为 {}-{} 个字符，支持大小写字母、数字、特殊字符，至少包含字母和数字", minLength, maxLength));
    }

    public static UserException passwordPolicyViolated(String message) {
        return of("USER_PASSWORD_POLICY_VIOLATED", message);
    }

    public static UserException avatarEmpty() {
        return of("USER_AVATAR_EMPTY", "头像不能为空");
    }

    public static UserException passwordIncorrect() {
        return of("USER_PASSWORD_INCORRECT", "当前密码不正确");
    }

    public static UserException passwordSameAsOld() {
        return of("USER_PASSWORD_SAME_AS_OLD", "新密码不能与当前密码相同");
    }

    public static UserException phoneSameAsOld() {
        return of("USER_PHONE_SAME_AS_OLD", "新手机号不能与当前手机号相同");
    }

    public static UserException emailSameAsOld() {
        return of("USER_EMAIL_SAME_AS_OLD", "新邮箱不能与当前邮箱相同");
    }

    public static UserException deleteSelfNotAllowed() {
        return of("USER_DELETE_SELF_NOT_ALLOWED", "不允许删除当前用户");
    }

    public static UserException builtinDeleteNotAllowed(String displayName) {
        return of("USER_BUILTIN_DELETE_NOT_ALLOWED", StrUtil.format("所选用户 [{}] 是系统内置用户，不允许删除", displayName));
    }

    public static UserException avatarFormatNotSupported(String supportSuffix) {
        return of("USER_AVATAR_FORMAT_NOT_SUPPORTED", StrUtil.format("头像仅支持 {} 格式的图片", supportSuffix));
    }

    public static UserException importDataInvalid() {
        return of("USER_IMPORT_DATA_INVALID", "数据文件格式不正确");
    }

    public static UserException importEmailDuplicate() {
        return of("USER_IMPORT_EMAIL_DUPLICATE", "存在重复邮箱，请检测数据");
    }

    public static UserException importPhoneDuplicate() {
        return of("USER_IMPORT_PHONE_DUPLICATE", "存在重复手机，请检测数据");
    }

    public static UserException importPolicyViolated() {
        return of("USER_IMPORT_POLICY_VIOLATED", "数据不符合导入策略，已退出导入");
    }

    public static UserException importRoleInvalid() {
        return of("USER_IMPORT_ROLE_INVALID", "存在无效角色，请检查数据");
    }

    public static UserException importDepartmentInvalid() {
        return of("USER_IMPORT_DEPARTMENT_INVALID", "存在无效部门，请检查部门名称或部门层级是否正确");
    }

    public static UserException departmentNamesEmpty() {
        return of("USER_DEPARTMENT_NAMES_EMPTY", "部门名称集合不能为空");
    }

    public static UserException departmentNameListEmpty() {
        return of("USER_DEPARTMENT_NAMES_EMPTY", "部门名称列表不能为空");
    }

    public static UserException departmentsInvalidOrAmbiguous(String departmentNames) {
        return of("USER_DEPARTMENT_INVALID_OR_AMBIGUOUS", StrUtil.format("以下部门无效或存在歧义：{}", departmentNames));
    }

    public static UserException departmentNotFoundOrAmbiguous(String departmentName) {
        return of("USER_DEPARTMENT_INVALID_OR_AMBIGUOUS", StrUtil.format("部门 [{}] 不存在或存在歧义", departmentName));
    }

    public static UserException departmentPathBlank() {
        return of("USER_DEPARTMENT_PATH_BLANK", "部门路径不能为空");
    }

    public static UserException departmentPathFormatInvalid(String departmentPath) {
        return of("USER_DEPARTMENT_PATH_FORMAT_INVALID", StrUtil.format("部门路径格式错误：{}", departmentPath));
    }

    public static UserException departmentPathContainsBlank(String departmentPath) {
        return of("USER_DEPARTMENT_PATH_FORMAT_INVALID", StrUtil.format("部门路径包含空名称：{}", departmentPath));
    }

    public static UserException departmentNotFoundInPath(String departmentName, String departmentPath) {
        return of("USER_DEPARTMENT_NOT_FOUND", StrUtil.format("找不到部门 [{}] 在路径 [{}] 中", departmentName, departmentPath));
    }

    public static UserException departmentNotFound(String departmentName) {
        return of("USER_DEPARTMENT_NOT_FOUND", StrUtil.format("部门 [{}] 不存在", departmentName));
    }

    public static UserException departmentNameDuplicate(String departmentName) {
        return of("USER_DEPARTMENT_NAME_DUPLICATE", StrUtil.format("存在多个同名部门 [{}]，请使用完整层级路径，如：公司名:{}", departmentName, departmentName));
    }

    public static UserException socialAlreadyBound(String source) {
        return of("USER_SOCIAL_ALREADY_BOUND", StrUtil.format("您已经绑定过了 [{}] 平台，请先解绑", source));
    }

    public static UserException socialBoundByOther(String source, String username) {
        return of("USER_SOCIAL_BOUND_BY_OTHER", StrUtil.format("[{}] 平台账号 [{}] 已被其他用户绑定", source, username));
    }

    public static UserException passwordPolicyInvalid(String message) {
        return of("USER_PASSWORD_POLICY_INVALID", message);
    }

    public static UserException passwordWarningDaysExceedExpiration() {
        return of("USER_PASSWORD_WARNING_DAYS_EXCEED_EXPIRATION", "密码到期提醒时间应小于密码有效期");
    }

    public static UserException kickoutSelfNotAllowed() {
        return of("AUTH_KICKOUT_SELF_NOT_ALLOWED", "不能强退自己");
    }

}
