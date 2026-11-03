
package top.wyhao.system.admin.adapter.web;

import cn.hutool.core.collection.CollUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import top.wyhao.system.admin.domain.exception.NoticeException;
import top.wyhao.system.admin.domain.model.NoticeMethods;
import top.wyhao.system.admin.domain.model.NoticeScopes;
import top.wyhao.system.admin.app.service.MessageService;
import top.wyhao.system.admin.app.service.NoticeService;
import top.wyhao.satoken.util.LoginUtil;
import top.wyhao.web.core.model.PageQuery;
import top.wyhao.web.core.model.IdsRequest;
import top.wyhao.mybatis.query.PageResult;

import java.util.Collections;
import java.util.List;
import top.wyhao.system.admin.app.dto.MessageDetailResult;
import top.wyhao.system.admin.app.dto.MessageQuery;
import top.wyhao.system.admin.app.dto.MessageResult;
import top.wyhao.system.admin.app.dto.MessageUnreadResult;
import top.wyhao.system.admin.app.dto.NotificationDetailResult;
import top.wyhao.system.admin.app.dto.NotificationQuery;
import top.wyhao.system.admin.app.dto.NotificationResult;
import top.wyhao.system.admin.app.dto.NotificationUnreadCountResult;

/**
 * 个人消息 API
 *

 * @since 2025/4/5 21:30
 */
@Tag(name = "个人消息 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/user/message")
public class UserMessageController {

    private final NoticeService noticeService;
    private final MessageService messageService;

    @Operation(summary = "查询未读消息数量", description = "查询当前用户的未读消息数量")
    @Parameter(name = "isDetail", description = "是否查询详情", example = "true", in = ParameterIn.QUERY)
    @GetMapping("/unread")
    public MessageUnreadResult countUnreadMessage(@RequestParam(required = false) Boolean detail) {
        return messageService.countUnreadByUserId(LoginUtil.getUserId(), detail);
    }

    @Operation(summary = "分页查询消息列表", description = "分页查询消息列表")
    @GetMapping
    public PageResult<MessageDetailResult> page(MessageQuery query, @Valid PageQuery pageQuery) {
        query.setUserId(LoginUtil.getUserId());
        return messageService.page(query, pageQuery);
    }

    @Operation(summary = "查询消息", description = "查询消息详情")
    @Parameter(name = "id", description = "ID", example = "1", in = ParameterIn.PATH)
    @GetMapping("/{id}")
    public MessageResult getMessage(@PathVariable Long id) {
        MessageResult detail = messageService.get(id);
        if (detail == null || (NoticeScopes.USER.equals(detail.getScope()) && !CollUtil
            .contains(detail.getUsers(), LoginUtil.getUserId().toString()))) {
            throw NoticeException.messageNotFoundOrNoAccess();
        }
        messageService.readMessage(Collections.singletonList(id), LoginUtil.getUserId());
        return detail;
    }

    @Operation(summary = "删除消息", description = "删除消息")
    @DeleteMapping
    public void delete(@RequestBody @Valid IdsRequest req) {
        messageService.delete(req.getIds());
    }

    @Operation(summary = "消息标记为已读", description = "将消息标记为已读状态")
    @PatchMapping("/read")
    public void read(@RequestBody @Valid IdsRequest req) {
        messageService.readMessage(req.getIds(), LoginUtil.getUserId());
    }

    @Operation(summary = "消息全部已读", description = "将所有消息标记为已读状态")
    @PatchMapping("/readAll")
    public void readAll() {
        messageService.readMessage(null, LoginUtil.getUserId());
    }

    @Operation(summary = "查询未读公告数量", description = "查询当前用户的未读公告数量")
    @GetMapping("/notice/unread")
    public NotificationUnreadCountResult countUnread() {
        List<Long> list = noticeService.listUnreadIdsByUserId(null, LoginUtil.getUserId());
        return new NotificationUnreadCountResult(list.size());
    }

    @Operation(summary = "查询未读公告", description = "查询当前用户的未读公告")
    @Parameter(name = "method", description = "通知方式", example = "LOGIN_POPUP", in = ParameterIn.PATH)
    @GetMapping("/notice/unread/{method}")
    public List<Long> listUnread(@PathVariable String method) {
        return noticeService.listUnreadIdsByUserId(NoticeMethods.valueOf(method), LoginUtil.getUserId());
    }

    @Operation(summary = "分页查询公告列表", description = "分页查询公告列表")
    @GetMapping("/notice")
    public PageResult<NotificationResult> pageNotice(NotificationQuery query, @Valid PageQuery pageQuery) {
        query.setUserId(LoginUtil.getUserId());
        return noticeService.page(query, pageQuery);
    }

    @Operation(summary = "查询公告", description = "查询公告详情")
    @Parameter(name = "id", description = "ID", example = "1", in = ParameterIn.PATH)
    @GetMapping("/notice/{id}")
    public NotificationDetailResult getNotice(@PathVariable Long id) {
        NotificationDetailResult detail = noticeService.detail(id);
        if (detail == null || (NoticeScopes.USER.equals(detail.getNoticeScope()) && !detail
            .getNoticeUsers()
            .contains(LoginUtil.getUserId().toString()))) {
            throw NoticeException.notFoundOrNoAccess();
        }
        noticeService.readNotice(id, LoginUtil.getUserId());
        return detail;
    }
}
