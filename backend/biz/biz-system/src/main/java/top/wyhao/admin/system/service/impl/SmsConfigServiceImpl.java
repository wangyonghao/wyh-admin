
package top.wyhao.admin.system.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.lang.tree.Tree;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.dromara.sms4j.core.factory.SmsFactory;
import org.dromara.sms4j.provider.config.BaseConfig;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.wyhao.admin.system.model.query.SmsConfigQuery;
import top.wyhao.admin.system.mapper.SmsConfigMapper;
import top.wyhao.starter.core.enums.StatusEnum;
import top.wyhao.admin.system.config.sms.SmsConfigUtil;
import top.wyhao.admin.system.entity.SysSmsConfig;
import top.wyhao.admin.system.model.bo.SmsConfigReq;
import top.wyhao.admin.system.model.vo.SmsConfigResp;
import top.wyhao.admin.system.service.SmsConfigService;
import top.wyhao.starter.core.util.validation.BizAssert;
import top.wyhao.starter.web.core.model.PageQuery;
import top.wyhao.starter.web.core.model.PageResult;
import top.wyhao.starter.web.core.model.SortQuery;
import top.wyhao.starter.web.core.model.LabelValueResult;

import java.util.List;

/**
 * 短信配置业务实现
 *
 * @author luoqiz
 * @since 2025/03/15 18:41
 */
@Service
@RequiredArgsConstructor
public class SmsConfigServiceImpl implements SmsConfigService {
    private final SmsConfigMapper baseMapper;

    public void afterCreate(SmsConfigReq req, SysSmsConfig entity) {
        this.load(entity);
    }

    public void afterUpdate(SmsConfigReq req, SysSmsConfig entity) {
        // 重新加载配置
        // 先卸载
        this.unload(entity.getId().toString());
        // 再加载
        this.load(entity);
    }

    public void afterDelete(List<Long> ids) {
        for (Long id : ids) {
            this.unload(id.toString());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void setDefaultConfig(Long id) {
        SysSmsConfig smsConfig = baseMapper.selectById(id);
        if (Boolean.TRUE.equals(smsConfig.getIsDefault())) {
            return;
        }
        // 启用状态才能设为默认配置
        BizAssert.throwIfEqual(StatusEnum.DISABLE, smsConfig.getStatus(), "请先启用所选配置");
        baseMapper.lambdaUpdate().eq(SysSmsConfig::getIsDefault, true).set(SysSmsConfig::getIsDefault, false).update();
        baseMapper.lambdaUpdate().eq(SysSmsConfig::getId, id).set(SysSmsConfig::getIsDefault, true).update();
    }

    @Override
    public SysSmsConfig getDefaultConfig() {
        return baseMapper.lambdaQuery()
            .eq(SysSmsConfig::getIsDefault, true)
            .eq(SysSmsConfig::getStatus, StatusEnum.ENABLE)
            .one();
    }

    @Override
    public PageResult<SmsConfigResp> findPage(SmsConfigQuery query, PageQuery pageQuery) {
        return null;
    }

    @Override
    public List<SmsConfigResp> list(SmsConfigQuery query, SortQuery sortQuery) {
        return List.of();
    }

    @Override
    public List<Tree<Long>> tree(SmsConfigQuery query, SortQuery sortQuery, boolean b) {
        return List.of();
    }

    @Override
    public Long create(SmsConfigReq req) {
        return 0L;
    }

    @Override
    public SmsConfigResp get(Long id) {
        return null;
    }

    @Override
    public void update(SmsConfigReq req, Long id) {

    }

    @Override
    public void delete(List<Long> id) {

    }

    @Override
    public void export(SmsConfigQuery query, SortQuery sortQuery, HttpServletResponse response) {

    }

    @Override
    public List<LabelValueResult> dict(SmsConfigQuery query, SortQuery sortQuery) {
        return List.of();
    }

    /**
     * 加载配置
     *
     * @param entity 配置信息
     */
    private void load(SysSmsConfig entity) {
        SysSmsConfig smsConfigDO = baseMapper.selectById(entity.getId());
        SmsConfigResp smsConfig = BeanUtil.copyProperties(smsConfigDO, SmsConfigResp.class);
        BaseConfig config = SmsConfigUtil.from(smsConfig);
        if (config != null) {
            SmsFactory.createSmsBlend(config);
        }
    }

    /**
     * 卸载配置
     *
     * @param configId 配置 ID
     */
    private void unload(String configId) {
        if (SmsFactory.getSmsBlend(configId) != null) {
            SmsFactory.unregister(configId);
        }
    }
}