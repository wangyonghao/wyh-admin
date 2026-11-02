package top.wyhao.system.admin.infrastructure.repository;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import top.wyhao.mybatis.query.PageResult;
import top.wyhao.mybatis.query.QueryWrapperBuilder;
import top.wyhao.system.admin.domain.gateway.ConfigRepository;
import top.wyhao.system.admin.domain.model.Config;
import top.wyhao.system.admin.infrastructure.persistence.AdminDoMapper;
import top.wyhao.system.admin.infrastructure.persistence.dataobject.SysConfigDO;
import top.wyhao.system.admin.infrastructure.repository.mapper.SysConfigMapper;

import java.util.List;

/**
 * 系统配置仓储实现
 */
@Repository
@RequiredArgsConstructor
public class ConfigRepositoryImpl implements ConfigRepository {

    private final SysConfigMapper configMapper;

    @Override
    public Config findById(Long id) {
        return AdminDoMapper.toDomain(configMapper.selectById(id), Config.class);
    }

    @Override
    public Config findByKey(String configKey) {
        return AdminDoMapper.toDomain(
                configMapper.lambdaQuery().eq(SysConfigDO::getConfigKey, configKey).one(),
                Config.class);
    }

    @Override
    public void insert(Config config) {
        SysConfigDO data = AdminDoMapper.toDo(config, SysConfigDO.class);
        configMapper.insert(data);
        AdminDoMapper.copyBack(data, config);
    }

    @Override
    public int updateById(Config config) {
        SysConfigDO data = AdminDoMapper.toDo(config, SysConfigDO.class);
        int rows = configMapper.updateById(data);
        AdminDoMapper.copyBack(data, config);
        return rows;
    }

    @Override
    public void deleteByIds(List<Long> ids) {
        configMapper.deleteByIds(ids);
    }

    @Override
    public List<Config> listByQuery(Object query) {
        return AdminDoMapper.toDomainList(
                configMapper.selectList(QueryWrapperBuilder.build(query, SysConfigDO.class)),
                Config.class);
    }

    @Override
    public PageResult<Config> page(long page, long pageSize, Object query) {
        return PageResult.of(configMapper.selectPage(
                        new Page<>(page, pageSize),
                        QueryWrapperBuilder.build(query, SysConfigDO.class)))
                .map(item -> AdminDoMapper.toDomain(item, Config.class));
    }

    @Override
    public boolean existsKey(String configKey, Long excludeId) {
        QueryWrapper<SysConfigDO> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("config_key", configKey);
        if (excludeId != null) {
            queryWrapper.ne("id", excludeId);
        }
        return configMapper.selectCount(queryWrapper) > 0;
    }

    @Override
    public <T> T getAs(String configKey, Class<T> type) {
        return configMapper.getConfig(configKey, type);
    }
}
