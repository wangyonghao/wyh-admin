package top.wyhao.system.admin.infrastructure.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import top.wyhao.system.admin.infrastructure.persistence.dataobject.SysJobLogDO;

@Mapper
public interface SysJobLogMapper extends BaseMapper<SysJobLogDO> {
}
