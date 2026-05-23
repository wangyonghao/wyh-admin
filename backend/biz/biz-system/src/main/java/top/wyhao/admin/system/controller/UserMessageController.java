
package top.wyhao.admin.system.controller;

import cn.hutool.core.collection.CollUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import top.wyhao.admin.system.model.enums.NoticeMethods;
import top.wyhao.admin.system.model.enums.NoticeScopes;
import top.wyhao.admin.system.model.query.MessageQuery;
import top.wyhao.admin.system.model.query.NoticeQuery;
import top.wyhao.admin.system.model.vo.message.MessageDetailResult;
import top.wyhao.admin.system.model.vo.message.MessageResult;
import top.wyhao.admin.system.model.vo.message.MessageUnreadResp;
import top.wyhao.admin.system.model.vo.NoticeDetailResult;
import top.wyhao.admin.system.model.vo.NoticeResult;
import top.wyhao.admin.system.model.vo.NoticeUnreadCountResult;
import top.wyhao.admin.system.service.MessageService;
import top.wyhao.admin.system.service.NoticeService;
import top.wyhao.common.security.util.LoginUtil;
import top.wyhao.starter.core.util.validation.BizAssert;
import top.wyhao.starter.web.core.model.PageQuery;
import top.wyhao.starter.web.core.model.IdsRequest;
import top.wyhao.starter.web.core.model.PageResult;

import java.util.Collections;
import java.util.List;

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
    public MessageUnreadResp countUnreadMessage(@RequestParam(required = false) Boolean detail) {
        return messageService.countUnreadByUserId(LoginUtil.getUserId(), detail);
    }

    @Operation(summary = "分页查询消息列表", description = "分页查询消息列表")
    @GetMapping
    public PageResult<MessageResult> page(@Valid MessageQuery query, @Valid PageQuery pageQuery) {
        query.setUserId(LoginUtil.getUserId());
        return messageService.page(query, pageQuery);
    }

    @Operation(summary = "查询消息", description = "查询消息详情")
    @Parameter(name = "id", description = "ID", example = "1", in = ParameterIn.PATH)
    @GetMapping("/{id}")
    public MessageDetailResult getMessage(@PathVariable Long id) {
        MessageDetailResult detail = messageService.get(id);
        BizAssert.isTrue(detail == null || (NoticeScopes.USER.equals(detail.getScope()) && !CollUtil
            .contains(detail.getUsers(), LoginUtil.getUserId().toString())), "消息不存在或无权限访问");
        messageService.readMessage(Collections.singletonList(id), LoginUtil.getUserId());
        detail.setIsRead(true);
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
    public NoticeUnreadCountResult countUnread() {
        List<Long> list = noticeService.listUnreadIdsByUserId(null, LoginUtil.getUserId());
        return new NoticeUnreadCountResult(list.size());
    }

    @Operation(summary = "查询未读公告", description = "查询当前用户的未读公告")
    @Parameter(name = "method", description = "通知方式", example = "LOGIN_POPUP", in = ParameterIn.PATH)
    @GetMapping("/notice/unread/{method}")
    public List<Long> listUnread(@PathVariable String method) {
        return noticeService.listUnreadIdsByUserId(NoticeMethods.valueOf(method), LoginUtil.getUserId());
    }

    @Operation(summary = "分页查询公告列表", description = "分页查询公告列表")
    @GetMapping("/notice")
    public PageResult<NoticeResult> pageNotice(@Valid NoticeQuery query, @Valid PageQuery pageQuery) {
        query.setUserId(LoginUtil.getUserId());
        return noticeService.page(query, pageQuery);
    }

    @Operation(summary = "查询公告", description = "查询公告详情")
    @Parameter(name = "id", description = "ID", example = "1", in = ParameterIn.PATH)
    @GetMapping("/notice/{id}")
    public NoticeDetailResult getNotice(@PathVariable Long id) {
        NoticeDetailResult detail = noticeService.detail(id);
        BizAssert.isTrue(detail == null || (NoticeScopes.USER.equals(detail.getNoticeScope()) && !detail
            .getNoticeUsers()
            .contains(LoginUtil.getUserId().toString())), "公告不存在或无权限访问");
        noticeService.readNotice(id, LoginUtil.getUserId());
        return detail;
    }
}
