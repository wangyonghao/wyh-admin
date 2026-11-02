package top.wyhao.system.admin.app.query;

import top.wyhao.mybatis.query.PageParam;
import top.wyhao.mybatis.query.PageResult;
import top.wyhao.system.admin.app.dto.UserQuery;
import top.wyhao.system.admin.app.dto.UserDetail;
import top.wyhao.system.admin.app.dto.UserResult;

import java.util.List;

/**
 * 用户列表读模型。分页和导出 SQL 直接映射到接口视图，因此端口放在应用层。
 */
public interface UserViewQuery {

    PageResult<UserResult> page(UserQuery query, PageParam pageParam);

    List<UserDetail> list(UserQuery query);
}
