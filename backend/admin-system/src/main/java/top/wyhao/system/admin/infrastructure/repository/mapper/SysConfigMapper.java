package top.wyhao.system.admin.infrastructure.repository.mapper;

import cn.hutool.core.text.CharSequenceUtil;
import org.apache.ibatis.annotations.Mapper;
import top.wyhao.mybatis.model.BaseMapper;
import top.wyhao.system.admin.infrastructure.persistence.dataobject.SysConfigDO;
import top.wyhao.web.json.util.JSONUtils;

/**
 * 系统配置 Mapper
 *
 * @since 2024/04/26
 */
@Mapper
public interface SysConfigMapper extends BaseMapper<SysConfigDO> {

    /**
     * 获取配置并转换为指定类型
     *
     * @param configKey 配置键
     * @param clazz     目标类型
     * @return 配置对象
     */
    default <T> T getConfig(String configKey, Class<T> clazz) {
        SysConfigDO configDO = lambdaQuery().eq(SysConfigDO::getConfigKey, configKey).one();

        if (configDO == null || CharSequenceUtil.isBlank(configDO.getConfigValue())) {
            return null;
        }

        return JSONUtils.toBean(configDO.getConfigValue(), clazz);
    }
}
