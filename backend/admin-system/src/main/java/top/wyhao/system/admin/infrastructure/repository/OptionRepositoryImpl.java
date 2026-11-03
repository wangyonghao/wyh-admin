package top.wyhao.system.admin.infrastructure.repository;

import cn.hutool.core.text.CharSequenceUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import top.wyhao.mybatis.query.PageFactory;
import top.wyhao.mybatis.query.PageParam;
import top.wyhao.mybatis.query.PageResult;
import top.wyhao.mybatis.query.QueryWrapperBuilder;
import top.wyhao.mybatis.query.SortableQuery;
import top.wyhao.mybatis.query.Sorts;
import top.wyhao.system.admin.domain.gateway.OptionRepository;
import top.wyhao.system.admin.domain.model.Option;
import top.wyhao.system.admin.infrastructure.persistence.AdminDoMapper;
import top.wyhao.system.admin.infrastructure.persistence.dataobject.SysOptionDO;
import top.wyhao.system.admin.infrastructure.repository.mapper.SysOptionMapper;

import java.util.List;

/**
 * 选项仓储实现
 */
@Repository
@RequiredArgsConstructor
public class OptionRepositoryImpl implements OptionRepository {

    private final SysOptionMapper optionMapper;

    @Override
    public Option findById(Long id) {
        return AdminDoMapper.toDomain(optionMapper.selectById(id), Option.class);
    }

    @Override
    public List<Option> findByIds(List<Long> ids) {
        return AdminDoMapper.toDomainList(optionMapper.selectByIds(ids), Option.class);
    }

    @Override
    public void insert(Option option) {
        SysOptionDO data = AdminDoMapper.toDo(option, SysOptionDO.class);
        optionMapper.insert(data);
        AdminDoMapper.copyBack(data, option);
    }

    @Override
    public void updateById(Option option) {
        SysOptionDO data = AdminDoMapper.toDo(option, SysOptionDO.class);
        optionMapper.updateById(data);
        AdminDoMapper.copyBack(data, option);
    }

    @Override
    public void deleteByIds(List<Long> ids) {
        optionMapper.deleteByIds(ids);
    }

    @Override
    public PageResult<Option> page(SortableQuery query, PageParam pageParam) {
        return PageResult.of(optionMapper.selectPage(
                        PageFactory.build(pageParam, query, SysOptionDO.class,
                                Sorts.asc(SysOptionDO::getOptionType).thenAsc(SysOptionDO::getSort)),
                        QueryWrapperBuilder.build(query, SysOptionDO.class)))
                .map(item -> AdminDoMapper.toDomain(item, Option.class));
    }

    @Override
    public List<Option> listEnabledByType(String type) {
        return AdminDoMapper.toDomainList(optionMapper.lambdaQuery()
                .eq(SysOptionDO::getOptionType, type)
                .eq(SysOptionDO::getEnabled, true)
                .orderByAsc(SysOptionDO::getSort)
                .orderByAsc(SysOptionDO::getId)
                .list(), Option.class);
    }

    @Override
    public List<String> listDistinctTypes() {
        return optionMapper.query()
                .select("DISTINCT option_type")
                .orderByAsc("option_type")
                .list()
                .stream()
                .map(SysOptionDO::getOptionType)
                .filter(CharSequenceUtil::isNotBlank)
                .toList();
    }

    @Override
    public boolean existsValue(String type, String value, Long excludeId) {
        return optionMapper.lambdaQuery()
                .eq(SysOptionDO::getOptionType, type)
                .eq(SysOptionDO::getValue, value)
                .ne(excludeId != null, SysOptionDO::getId, excludeId)
                .exists();
    }

    @Override
    public long countIdGreaterThan(Long id) {
        return optionMapper.lambdaQuery().gt(SysOptionDO::getId, id).count();
    }

    @Override
    public boolean deleteIdGreaterThan(Long id) {
        return optionMapper.lambdaUpdate().gt(SysOptionDO::getId, id).remove();
    }
}
