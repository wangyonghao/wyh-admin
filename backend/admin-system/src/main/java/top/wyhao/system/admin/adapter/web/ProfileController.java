
package top.wyhao.system.admin.adapter.web;

import com.xkcoding.justauth.AuthRequestFactory;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import me.zhyd.oauth.model.AuthCallback;
import me.zhyd.oauth.model.AuthResponse;
import me.zhyd.oauth.model.AuthUser;
import me.zhyd.oauth.request.AuthRequest;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import top.wyhao.system.admin.app.service.PasswordService;
import top.wyhao.system.admin.domain.model.UserSocial;
import top.wyhao.system.admin.domain.exception.UserException;
import top.wyhao.system.admin.app.dto.UserBasicInfoUpdateReq;
import top.wyhao.system.admin.domain.model.SocialSource;
import top.wyhao.system.admin.app.dto.UserSocialBindResp;
import top.wyhao.system.admin.app.service.UserProfileService;
import top.wyhao.system.admin.app.service.UserSocialService;
import top.wyhao.satoken.util.LoginUtil;
import top.wyhao.system.auth.domain.exception.AuthenticationException;
import top.wyhao.system.auth.adapter.client.ContactCaptchaApi;
import top.wyhao.cmn.core.util.CollUtils;
import top.wyhao.cmn.core.util.RsaUtils;

import java.io.IOException;
import java.util.List;
import top.wyhao.system.admin.app.dto.ProfileAvatarResult;
import top.wyhao.system.admin.app.dto.ProfileEmailUpdateRequest;
import top.wyhao.system.admin.app.dto.ProfilePasswordUpdateRequest;
import top.wyhao.system.admin.app.dto.ProfilePhoneUpdateRequest;

/**
 * 个人信息 API
 *

 * @since 2023/1/2 11:41
 */
@Tag(name = "个人信息 API")
@Validated
@RestController
@RequiredArgsConstructor
public class ProfileController {

    private static final String DECRYPT_FAILED = "当前密码解密失败";
    private final UserProfileService userProfileService;
    private final UserSocialService userSocialService;
    private final AuthRequestFactory authRequestFactory;
    private final PasswordService passwordService;
    private final ContactCaptchaApi contactCaptchaApi;

    @Operation(summary = "修改头像", description = "用户修改个人头像")
    @PatchMapping("/user/profile/avatar")
    public ProfileAvatarResult updateAvatar(@NotNull(message = "头像不能为空") MultipartFile avatarFile) throws IOException {
        if (avatarFile.isEmpty()) {
            throw UserException.avatarEmpty();
        }
        Long newAvatar = userProfileService.updateAvatar(avatarFile, LoginUtil.getUserId());
        return new ProfileAvatarResult(newAvatar);
    }

    @Operation(summary = "修改基础信息", description = "修改用户基础信息")
    @PatchMapping("/user/profile/basic/info")
    public void updateBasicInfo(@RequestBody @Valid UserBasicInfoUpdateReq req) {
        userProfileService.updateBasicInfo(req, LoginUtil.getUserId());
    }

    @Operation(summary = "修改密码", description = "修改用户登录密码")
    @PatchMapping("/user/profile/password")
    public void updatePassword(@RequestBody @Valid ProfilePasswordUpdateRequest updateReq) {
        String oldPassword = RsaUtils.decryptPasswordByRsaPrivateKey(updateReq.getOldPassword(), DECRYPT_FAILED);
        String newPassword = RsaUtils.decryptPasswordByRsaPrivateKey(updateReq.getNewPassword(), "新密码解密失败");
        passwordService.changePassword(LoginUtil.getUserId(), oldPassword, newPassword);
    }

    @Operation(summary = "修改手机号", description = "修改手机号")
    @PatchMapping("/user/profile/phone")
    public void updatePhone(@RequestBody @Valid ProfilePhoneUpdateRequest updateReq) {
        String oldPassword = RsaUtils.decryptPasswordByRsaPrivateKey(updateReq.getOldPassword(), DECRYPT_FAILED);
        contactCaptchaApi.verifyPhone(updateReq.getPhone(), updateReq.getCaptcha());
        userProfileService.updatePhone(updateReq.getPhone(), oldPassword, LoginUtil.getUserId());
    }

    @Operation(summary = "修改邮箱", description = "修改用户邮箱")
    @PatchMapping("/user/profile/email")
    public void updateEmail(@RequestBody @Valid ProfileEmailUpdateRequest request) {
        String oldPassword = RsaUtils.decryptPasswordByRsaPrivateKey(request.getOldPassword(), DECRYPT_FAILED);
        contactCaptchaApi.verifyEmail(request.getEmail(), request.getCaptcha());
        userProfileService.updateEmail(request.getEmail(), oldPassword, LoginUtil.getUserId());
    }

    @Operation(summary = "查询绑定的三方账号", description = "查询绑定的三方账号")
    @GetMapping("/user/profile/social")
    public List<UserSocialBindResp> listSocialBind() {
        List<UserSocial> userSocialList = userSocialService.listByUserId(LoginUtil.getUserId());
        return CollUtils.mapToList(userSocialList, userSocial -> {
            String source = userSocial.getSource();
            UserSocialBindResp userSocialBind = new UserSocialBindResp();
            userSocialBind.setSource(source);
            userSocialBind.setDescription(SocialSource.valueOf(source).getDescription());
            return userSocialBind;
        });
    }

    @Operation(summary = "绑定三方账号", description = "绑定三方账号")
    @Parameter(name = "provider", description = "来源", example = "gitee", in = ParameterIn.PATH)
    @PostMapping("/user/profile/social/{provider}")
    public void bindSocial(@PathVariable String source, @RequestBody AuthCallback callback) {
        AuthRequest authRequest = this.getAuthRequest(source);
        AuthResponse<AuthUser> response = authRequest.login(callback);
        if (!response.ok()) {
            throw AuthenticationException.socialAuthFailed(response.getMsg());
        }
        AuthUser authUser = response.getData();
        userSocialService.bind(authUser, LoginUtil.getUserId());
    }

    @Operation(summary = "解绑三方账号", description = "解绑三方账号")
    @Parameter(name = "provider", description = "来源", example = "gitee", in = ParameterIn.PATH)
    @DeleteMapping("/user/profile/social/{provider}")
    public void unbindSocial(@PathVariable String source) {
        userSocialService.deleteBySourceAndUserId(source, LoginUtil.getUserId());
    }

    private AuthRequest getAuthRequest(String source) {
        try {
            return authRequestFactory.get(source);
        } catch (Exception e) {
            throw AuthenticationException.platformNotSupport(source);
        }
    }
}
