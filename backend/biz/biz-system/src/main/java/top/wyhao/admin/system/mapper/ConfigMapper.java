
package top.wyhao.admin.system.mapper;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import top.wyhao.admin.system.entity.SysConfig;
import top.wyhao.admin.system.model.vo.ConfigResult;
import top.wyhao.admin.system.service.impl.ConfigServiceImpl;
import top.wyhao.cmn.db.model.BaseMapper;
import top.wyhao.starter.core.exception.BusinessException;

/**
 * 系统配置 Mapper
 *
 * @author wyhao
 * @since 2024/04/26
 */
@Mapper
public interface ConfigMapper extends BaseMapper<SysConfig> {

    /**
     * 分页查询
     *
     * @param page         分页对象
     * @param queryWrapper 查询条件
     * @return 分页结果
     */
    IPage<ConfigResult> selectConfigPage(IPage<ConfigResult> page, @Param(Constants.WRAPPER) Wrapper<SysConfig> queryWrapper);

    /**
     * 获取配置并转换为指定类型
     *
     * @param configKey 配置键
     * @param clazz     目标类型
     * @return 配置对象
     */
    default <T> T getConfig(String configKey, Class<T> clazz) {
        SysConfig configDO = lambdaQuery().eq(SysConfig::getConfigKey, configKey).one();

        if (configDO == null || CharSequenceUtil.isBlank(configDO.getConfigValue())) {
            try {
                return clazz.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                throw new BusinessException("获取配置失败");
            }
        }
        return JSONUtil.toBean(configDO.getConfigValue(), clazz);
    }
}
