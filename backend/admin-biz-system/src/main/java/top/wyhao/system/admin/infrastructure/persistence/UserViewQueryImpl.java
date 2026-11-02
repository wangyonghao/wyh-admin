package top.wyhao.system.admin.infrastructure.persistence;

import top.wyhao.system.admin.infrastructure.persistence.dataobject.SysUserDO;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import top.wyhao.system.admin.infrastructure.persistence.AdminDoMapper;
import top.wyhao.mybatis.query.PageFactory;
import top.wyhao.mybatis.query.PageParam;
import top.wyhao.mybatis.query.PageResult;
import top.wyhao.mybatis.query.QueryWrapperBuilder;
import top.wyhao.system.admin.app.dto.UserQuery;
import top.wyhao.system.admin.app.dto.UserDetail;
import top.wyhao.system.admin.app.dto.UserResult;
import top.wyhao.system.admin.app.query.UserViewQuery;
import top.wyhao.system.admin.domain.model.User;
import top.wyhao.system.admin.infrastructure.persistence.mapper.SysUserMapper;

import java.util.List;

/**
 * 用户列表读模型实现。数据权限仍由 Mapper 上的注解生效。
 */
@Repository
@RequiredArgsConstructor
public class UserViewQueryImpl implements UserViewQuery {

    private final SysUserMapper userMapper;

    @Override
    public PageResult<UserResult> page(UserQuery query, PageParam pageParam) {
        LambdaQueryWrapper<SysUserDO> wrapper = QueryWrapperBuilder.build(query, SysUserDO.class);
        IPage<UserResult> page = userMapper.selectUserPage(PageFactory.build(pageParam, query, SysUserDO.class), wrapper);
        return PageResult.of(page);
    }

    @Override
    public List<UserDetail> list(UserQuery query) {
        return userMapper.selectUserList(QueryWrapperBuilder.build(query, SysUserDO.class));
    }
}
