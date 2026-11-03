package top.wyhao.system.admin.app.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@Schema(description = "当前用户组织会话")
public class OrgSessionResult {
    private OrgResult current;
    private List<OrgResult> organizations = new ArrayList<>();
    private boolean needsOrganization;
    private List<OrgInviteResult> pendingInvites = new ArrayList<>();
}
