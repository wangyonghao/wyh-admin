package top.wyhao.system.admin.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import top.wyhao.system.admin.infrastructure.persistence.dataobject.SysJobDO;

@Mapper
public interface SysJobMapper extends BaseMapper<SysJobDO> {
}
