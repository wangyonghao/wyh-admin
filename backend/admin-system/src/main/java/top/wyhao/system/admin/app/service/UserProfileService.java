package top.wyhao.system.admin.app.service;

import org.springframework.web.multipart.MultipartFile;
import top.wyhao.system.admin.app.dto.UserBasicInfoUpdateReq;
import top.wyhao.system.admin.domain.model.User;

import java.util.List;

/**
 * 用户对自己账号的操作（个人中心 / 自助注册）。
 */
public interface UserProfileService {

    Long updateAvatar(MultipartFile avatar, Long userId);

    void updateBasicInfo(UserBasicInfoUpdateReq req, Long userId);

    void updatePhone(String newPhone, String oldPassword, Long userId);

    void updateEmail(String newEmail, String oldPassword, Long userId);

    /**
     * 自助注册：写入已校验的明文密码与角色。
     */
    User registerLocal(String username, String rawPassword, Long departmentId, List<Long> roleIds);

    /**
     * 邮箱自助注册：分配内部用户名后写入邮箱与慢哈希密码。
     */
    User registerWithEmail(String email, String rawPassword, Long departmentId, List<Long> roleIds);

    /**
     * 邮件链接确认后建号。密码已经是慢哈希，这里不再编码。
     */
    User createVerifiedEmailAccount(String username, String email, String passwordHash, Long departmentId, Long roleId);
}
