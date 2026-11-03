package top.wyhao.system.admin.app.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import top.wyhao.mybatis.query.PageResult;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.wyhao.settings.client.MailConfigVO;
import top.wyhao.mybatis.query.PageResult;
import top.wyhao.system.admin.app.dto.ConfigQuery;
import top.wyhao.system.admin.app.dto.ConfigRequest;
import top.wyhao.system.admin.app.dto.ConfigResult;
import top.wyhao.system.admin.app.dto.config.RegisterConfigVO;
import top.wyhao.system.admin.app.dto.config.StorageConfigVO;
import top.wyhao.system.admin.app.assembler.ConfigAssembler;
import top.wyhao.system.admin.app.service.ConfigService;
import top.wyhao.settings.client.ConfigKeys;
import top.wyhao.settings.client.ConfigStoreApi;
import top.wyhao.system.admin.domain.exception.ConfigException;
import top.wyhao.system.admin.domain.gateway.ConfigRepository;
import top.wyhao.system.admin.domain.model.Config;
import top.wyhao.starter.excel.util.ExcelUtils;
import top.wyhao.web.core.model.PageQuery;

import java.util.List;

/**
 * 系统配置业务实现。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ConfigServiceImpl implements ConfigService {

    private final ConfigRepository configRepository;
    private final ConfigAssembler configAssembler;
    private final ConfigStoreApi configStoreApi;

    @Override
    public PageResult<ConfigResult> page(ConfigQuery query, PageQuery pageQuery) {
        return configRepository.page(pageQuery.getPage(), pageQuery.getPageSize(), query)
                .map(configAssembler::toResult);
    }

    @Override
    public ConfigResult detail(Long id) {
        Config configDO = configRepository.findById(id);
        if (configDO == null) {
            throw ConfigException.notFound();
        }
        return configAssembler.toResult(configDO);
    }

    @Override
    public ConfigResult getByKey(String configKey) {
        Config configDO = configRepository.findByKey(configKey);
        if (configDO == null) {
            throw ConfigException.notFound();
        }
        return configAssembler.toResult(configDO);
    }

    @Override
    public RegisterConfigVO getRegisterConfig() {
        return configStoreApi.get(ConfigKeys.REGISTER, RegisterConfigVO.class);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateRegisterConfig(RegisterConfigVO config) {
        configStoreApi.put(ConfigKeys.REGISTER, config);
    }

    @Override
    public MailConfigVO getMailConfig() {
        return configStoreApi.get(ConfigKeys.MAIL, MailConfigVO.class);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateMailConfig(MailConfigVO config) {
        configStoreApi.put(ConfigKeys.MAIL, config);
    }

    public void checkMailConfig(MailConfigVO mailConfig) {
        if (mailConfig == null) {
            throw ConfigException.mailConfigNotFound();
        }
        if (CharSequenceUtil.isBlank(mailConfig.getHost())) {
            throw ConfigException.mailHostNotConfigured();
        }
        if (CharSequenceUtil.isBlank(mailConfig.getUsername())) {
            throw ConfigException.mailUsernameNotConfigured();
        }
        if (CharSequenceUtil.isBlank(mailConfig.getPassword())) {
            throw ConfigException.mailPasswordNotConfigured();
        }
    }

    @Override
    public StorageConfigVO getStorageConfig() {
        return configStoreApi.get(ConfigKeys.STORAGE, StorageConfigVO.class);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStorageConfig(StorageConfigVO config) {
        configStoreApi.put(ConfigKeys.STORAGE, config);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(ConfigRequest request) {
        this.checkUnique(request.getConfigKey(), null);
        Config configDO = configAssembler.toEntity(request);
        configRepository.insert(configDO);
        return configDO.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, ConfigRequest request) {
        Config oldConfig = configRepository.findById(id);
        if (oldConfig == null) {
            throw ConfigException.notFound();
        }
        if (CharSequenceUtil.isNotBlank(request.getConfigKey())) {
            this.checkUnique(request.getConfigKey(), id);
        }
        Config configDO = configAssembler.toEntity(request);
        configDO.setId(id);
        int updated = configRepository.updateById(configDO);
        if (updated <= 0) {
            throw ConfigException.updateConflict();
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateByKey(String configKey, ConfigRequest request) {
        Config existConfig = configRepository.findByKey(configKey);
        if (existConfig == null) {
            throw ConfigException.notFound();
        }
        Config configDO = new Config();
        configDO.setId(existConfig.getId());
        configDO.setConfigValue(request.getConfigValue());
        configDO.setDescription(request.getDescription());
        int updated = configRepository.updateById(configDO);
        if (updated <= 0) {
            throw ConfigException.updateConflict();
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(List<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return;
        }
        configRepository.deleteByIds(ids);
    }

    @Override
    public void export(ConfigQuery query, HttpServletResponse response) {
        List<Config> list = configRepository.listByQuery(query);
        List<ConfigResult> resultList = configAssembler.toResultList(list);
        ExcelUtils.export(resultList, "系统配置", ConfigResult.class, response);
    }

    private void checkUnique(String configKey, Long id) {
        if (configRepository.existsKey(configKey, id)) {
            throw ConfigException.keyExists();
        }
    }
}
