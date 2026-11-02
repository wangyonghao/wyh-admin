package top.wyhao.system.admin.app.assembler;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import top.wyhao.system.admin.app.dto.UserDetail;
import top.wyhao.system.admin.app.dto.UserRequest;
import top.wyhao.system.admin.domain.model.CreateUserCommand;
import top.wyhao.system.admin.domain.model.User;
import top.wyhao.cmn.identity.LoginUser;
import top.wyhao.web.convert.BaseEnumConverters;
import top.wyhao.web.convert.MapStructConfig;

/**
 * 用户对象转换
 */
@Mapper(config = MapStructConfig.class, uses = BaseEnumConverters.class)
public interface UserAssembler {

    @Mapping(source = "pwdUpdateTime", target = "pwdResetTime")
    UserDetail toDetail(User user);

    User toEntity(UserRequest request);

    @Mapping(source = "pwdResetTime", target = "pwdUpdateTime")
    User toEntity(UserDetail detail);

    @Mapping(source = "id", target = "userId")
    @Mapping(source = "pwdUpdateTime", target = "pwdResetTime")
    LoginUser toLoginUser(User user);

    /**
     * Web 请求 → 创建用户领域命令（含角色等跨聚合编排字段）。
     */
    @Mapping(source = "password", target = "rawPassword")
    @Mapping(target = "avatar", ignore = true)
    CreateUserCommand toCreateCommand(UserRequest request);
}
