package top.wyhao.system.admin.infrastructure.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import top.wyhao.system.admin.infrastructure.persistence.dataobject.SysFileDO;

/**
 * 文件 Mapper
 *
 * @author wyh
 * @since 2026/09/16
 */
@Mapper
public interface FileMapper extends BaseMapper<SysFileDO> {

}
