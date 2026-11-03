package top.wyhao.system.admin.app.service;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.web.multipart.MultipartFile;
import top.wyhao.mybatis.query.PageParam;
import top.wyhao.mybatis.query.PageResult;
import top.wyhao.system.admin.app.dto.UserImportRequest;
import top.wyhao.system.admin.app.dto.UserQuery;
import top.wyhao.system.admin.app.dto.UserRequest;
import top.wyhao.system.admin.app.dto.UserRoleUpdateReq;
import top.wyhao.system.admin.app.dto.UserDetail;
import top.wyhao.system.admin.app.dto.UserImportParseResp;
import top.wyhao.system.admin.app.dto.UserImportResp;
import top.wyhao.system.admin.app.dto.UserResult;
import top.wyhao.system.admin.domain.model.User;
import top.wyhao.web.core.model.LabelValueResult;

import java.util.List;

/**
 * 管理员对用户的管理操作。
 */
public interface UserAdminService {

    UserImportParseResp parseImport(MultipartFile file);

    UserImportResp importUser(UserImportRequest req);

    void updateRole(UserRoleUpdateReq updateReq, Long id);

    User getById(Long id);

    User getByUsername(String username);

    User getByPhone(String phone);

    User getByEmail(String email);

    Long countByDepartmentIds(List<Long> departmentIds);

    UserDetail detail(Long id);

    PageResult<UserResult> page(UserQuery query, PageParam pageParam);

    Long save(User user);

    Long create(@Valid UserRequest req);

    void export(@Valid UserQuery query, HttpServletResponse response);

    void update(Long id, @Valid UserRequest req);

    void delete(List<Long> ids);

    List<LabelValueResult<Long>> dict(UserQuery query);
}
