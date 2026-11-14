package top.wyhao.system.admin.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import top.wyhao.system.admin.infrastructure.persistence.dataobject.SysEmailRegistrationDO;

/**
 * 待验证注册。按邮箱查询必须带上加密 TypeHandler，否则对不上密文。
 */
@Mapper
public interface SysEmailRegistrationMapper extends BaseMapper<SysEmailRegistrationDO> {

    @Select("""
            SELECT id FROM sys_email_registration
            WHERE tenant_id = #{tenantId}
              AND consumed_at IS NULL
              AND email = #{email, typeHandler=top.wyhao.mybatis.encrypt.EncryptTypeHandler}
            """)
    Long findOpenId(@Param("tenantId") Long tenantId, @Param("email") String email);

    @Select("""
            SELECT id FROM sys_email_registration
            WHERE token_hash = #{tokenHash}
            FOR UPDATE
            """)
    Long lockIdByTokenHash(@Param("tokenHash") String tokenHash);
}
