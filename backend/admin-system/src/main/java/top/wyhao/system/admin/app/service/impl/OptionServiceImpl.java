package top.wyhao.system.admin.app.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.alicp.jetcache.anno.CacheInvalidate;
import com.alicp.jetcache.anno.Cached;
import top.wyhao.mybatis.query.PageResult;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.wyhao.cmn.core.constant.CacheConstants;
import top.wyhao.cmn.core.constant.StringConstants;
import top.wyhao.cmn.core.util.CollUtils;
import top.wyhao.mybatis.query.PageParam;
import top.wyhao.mybatis.query.PageResult;
import top.wyhao.system.admin.app.dto.OptionQuery;
import top.wyhao.system.admin.app.dto.OptionRequest;
import top.wyhao.system.admin.app.dto.OptionResult;
import top.wyhao.system.admin.app.service.OptionService;
import top.wyhao.system.admin.domain.exception.OptionException;
import top.wyhao.system.admin.domain.gateway.OptionRepository;
import top.wyhao.system.admin.domain.model.Option;
import top.wyhao.redisson.util.RedisUtils;
import top.wyhao.web.core.model.LabelValueResult;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * 选项业务实现
 */
@Service
@RequiredArgsConstructor
public class OptionServiceImpl implements OptionService {

    private final OptionRepository optionRepository;
    private final ObjectProvider<OptionService> self;

    @Override
    public PageResult<OptionResult> page(OptionQuery query, PageParam pageParam) {
        return optionRepository.page(query, pageParam).map(this::toResult);
    }

    @Override
    public OptionResult detail(Long id) {
        return toResult(require(id));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(OptionRequest request) {
        checkValueUnique(request.getOptionType(), request.getValue(), null);
        Option entity = toEntity(request);
        if (entity.getSort() == null) {
            entity.setSort(0);
        }
        if (entity.getEnabled() == null) {
            entity.setEnabled(true);
        }
        optionRepository.insert(entity);
        evict(entity.getOptionType());
        return entity.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, OptionRequest request) {
        Option old = require(id);
        checkValueUnique(request.getOptionType(), request.getValue(), id);
        Option entity = toEntity(request);
        entity.setId(id);
        optionRepository.updateById(entity);
        evict(old.getOptionType());
        evict(request.getOptionType());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(List<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            throw OptionException.deleteIdsEmpty();
        }
        List<Option> options = optionRepository.findByIds(ids);
        optionRepository.deleteByIds(ids);
        Set<String> types = new LinkedHashSet<>();
        for (Option option : options) {
            types.add(option.getOptionType());
        }
        types.forEach(this::evict);
    }

    @Override
    @CacheInvalidate(name = CacheConstants.OPTION_KEY_PREFIX, key = "#type")
    public void clearCache(String type) {
        RedisUtils.deleteByPattern(CacheConstants.OPTION_KEY_PREFIX + type + StringConstants.ASTERISK);
    }

    @Override
    @Cached(key = "#type", name = CacheConstants.OPTION_KEY_PREFIX)
    public List<LabelValueResult<String>> listByType(String type) {
        if (CharSequenceUtil.isBlank(type)) {
            return List.of();
        }
        List<Option> list = optionRepository.listEnabledByType(type);
        return list.stream()
                .map(item -> new LabelValueResult<>(item.getLabel(), item.getValue(), item.getExt()))
                .toList();
    }

    @Override
    public List<LabelValueResult<String>> listTypes() {
        return CollUtils.mapToList(optionRepository.listDistinctTypes(), type -> new LabelValueResult<>(type, type));
    }

    private Option require(Long id) {
        Option option = optionRepository.findById(id);
        if (option == null) {
            throw OptionException.notFound(id);
        }
        return option;
    }

    private void checkValueUnique(String type, String value, Long excludeId) {
        if (optionRepository.existsValue(type, value, excludeId)) {
            throw OptionException.valueExist(type, value);
        }
    }

    private void evict(String type) {
        if (CharSequenceUtil.isNotBlank(type)) {
            self.getObject().clearCache(type);
        }
    }

    private Option toEntity(OptionRequest request) {
        Option entity = new Option();
        entity.setOptionType(request.getOptionType());
        entity.setValue(request.getValue());
        entity.setLabel(request.getLabel());
        entity.setExt(request.getExtra());
        entity.setSort(request.getSort());
        entity.setEnabled(request.getEnabled());
        entity.setDescription(request.getDescription());
        return entity;
    }

    private OptionResult toResult(Option entity) {
        return new OptionResult(
                entity.getId(),
                entity.getOptionType(),
                entity.getValue(),
                entity.getLabel(),
                entity.getExt(),
                entity.getSort(),
                entity.getEnabled(),
                entity.getDescription());
    }
}
