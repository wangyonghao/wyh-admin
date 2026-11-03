package top.wyhao.system.admin.adapter.web;

import cn.dev33.satoken.annotation.SaIgnore;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import top.wyhao.system.admin.app.dto.OrgApplicationReviewRequest;
import top.wyhao.system.admin.app.dto.OrgApplyRequest;
import top.wyhao.system.admin.app.dto.OrgCreateRequest;
import top.wyhao.system.admin.app.dto.OrgInviteRequest;
import top.wyhao.system.admin.app.dto.OrgSwitchRequest;
import top.wyhao.system.admin.app.dto.OrgInviteResult;
import top.wyhao.system.admin.app.dto.OrgResult;
import top.wyhao.system.admin.app.dto.OrgSessionResult;
import top.wyhao.system.admin.app.service.OrgService;

@Tag(name = "组织 API")
@RestController
@RequiredArgsConstructor
public class OrganizationController {

    private final OrgService orgService;

    @Operation(summary = "当前用户组织会话")
    @GetMapping("/org/session")
    public OrgSessionResult session() {
        return orgService.currentSession();
    }

    @Operation(summary = "创建组织")
    @PostMapping("/org")
    public OrgResult create(@RequestBody @Valid OrgCreateRequest request) {
        return orgService.create(request);
    }

    @Operation(summary = "切换组织")
    @PostMapping("/org/switch")
    public void switchOrg(@RequestBody @Valid OrgSwitchRequest request) {
        orgService.switchOrg(request.getOrgId());
    }

    @Operation(summary = "申请加入组织")
    @PostMapping("/org/applications")
    public void apply(@RequestBody @Valid OrgApplyRequest request) {
        orgService.apply(request);
    }

    @Operation(summary = "审核加入申请")
    @PostMapping("/org/applications/{id}/review")
    public void review(@PathVariable("id") Long id, @RequestBody @Valid OrgApplicationReviewRequest request) {
        orgService.reviewApplication(id, Boolean.TRUE.equals(request.getApprove()));
    }

    @Operation(summary = "邀请用户加入当前组织")
    @PostMapping("/org/invites")
    public OrgInviteResult invite(@RequestBody @Valid OrgInviteRequest request) {
        return orgService.invite(request);
    }

    @SaIgnore
    @Operation(summary = "预览邀请（可不登录）")
    @GetMapping("/org/invites/preview")
    public OrgInviteResult preview(@RequestParam("token") String token) {
        return orgService.previewInvite(token);
    }

    @Operation(summary = "接受邀请")
    @PostMapping("/org/invites/accept")
    public void accept(@RequestParam("token") String token) {
        orgService.acceptInvite(token);
    }

    @Operation(summary = "拒绝邀请")
    @PostMapping("/org/invites/reject")
    public void reject(@RequestParam("token") String token) {
        orgService.rejectInvite(token);
    }
}
