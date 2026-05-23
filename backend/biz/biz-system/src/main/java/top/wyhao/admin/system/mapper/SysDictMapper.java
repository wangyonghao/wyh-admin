
package top.wyhao.admin.system.mapper;

import com.alicp.jetcache.anno.Cached;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import top.wyhao.admin.system.entity.SysDict;
import top.wyhao.cmn.db.model.BaseMapper;
import top.wyhao.starter.core.constant.CacheConstants;
import top.wyhao.starter.web.core.model.LabelValueResult;

import java.util.List;

/**
 * 字典 Mapper
 *

 * @since 2023/9/11 21:29
 */
@Mapper
public interface SysDictMapper extends BaseMapper<SysDict> {

    /**
     * 根据字典类型查询字典列表
     *
     * @param dictType 字典类型
     * @return 字典列表
     */
    @Cached(key = "#dictType", name = CacheConstants.DICT_KEY_PREFIX)
    List<LabelValueResult<String>> listByDictType(@Param("dictType") String dictType);
}