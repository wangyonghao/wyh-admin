
package top.wyhao.system.open.service.impl;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.wyhao.system.open.assembler.AppAssembler;
import top.wyhao.system.open.mapper.SysAppMapper;
import top.wyhao.system.open.model.entity.SysApp;
import top.wyhao.system.open.model.query.AppQuery;
import top.wyhao.system.open.model.req.AppReq;
import top.wyhao.system.open.model.resp.AppDetailResp;
import top.wyhao.system.open.model.resp.AppResult;
import top.wyhao.system.open.model.resp.AppSecretResp;
import top.wyhao.system.open.service.AppService;
import top.wyhao.mybatis.query.PageFactory;
import top.wyhao.mybatis.query.PageParam;
import top.wyhao.mybatis.query.PageResult;
import top.wyhao.mybatis.query.QueryWrapperBuilder;
import top.wyhao.cmn.core.constant.StringConstants;
import top.wyhao.starter.excel.util.ExcelUtils;

import java.util.List;

/**
 * 应用业务实现
 *
 * @since 2024/10/17 16:03
 */
@Service
@RequiredArgsConstructor
public class AppServiceImpl implements AppService {

    private final SysAppMapper baseMapper;
    private final AppAssembler appAssembler;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(AppReq req) {
        req.setAccessKey(Base64.encode(IdUtil.fastSimpleUUID())
                .replace(StringConstants.SLASH, StringConstants.EMPTY)
                .replace(StringConstants.PLUS, StringConstants.EMPTY)
                .substring(0, 30));
        req.setSecretKey(this.generateSecret());

        SysApp entity = appAssembler.toEntity(req);
        baseMapper.insert(entity);
        return entity.getId();
    }


    @Override
    public AppSecretResp getSecret(Long id) {
        SysApp app = baseMapper.selectById(id);
        AppSecretResp appSecretResp = new AppSecretResp();
        appSecretResp.setAccessKey(app.getAccessKey());
        appSecretResp.setSecretKey(app.getSecretKey());
        return appSecretResp;
    }

    @Override
    public void resetSecret(Long id) {
        SysApp app = new SysApp();
        app.setSecretKey(this.generateSecret());
        baseMapper.update(app, Wrappers.lambdaQuery(SysApp.class).eq(SysApp::getId, id));
    }

    @Override
    public SysApp getByAccessKey(String accessKey) {
        return baseMapper.selectByAccessKey(accessKey);
    }

    /**
     * 生成密钥
     *
     * @return 密钥
     */
    private String generateSecret() {
        return Base64.encode(IdUtil.fastSimpleUUID())
                .replace(StringConstants.SLASH, StringConstants.EMPTY)
                .replace(StringConstants.PLUS, StringConstants.EMPTY);
    }

    // 实现 CrudService 的其他方法
    @Override
    public PageResult<AppResult> page(AppQuery query, PageParam pageParam) {
        IPage<SysApp> page = baseMapper.selectPage(PageFactory.build(pageParam, query, SysApp.class),
                QueryWrapperBuilder.build(query, SysApp.class));
        return PageResult.of(page).map(appAssembler::toResult);
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(AppReq req, Long id) {
        SysApp entity = baseMapper.selectById(id);
        appAssembler.update(req, entity);
        baseMapper.updateById(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(List<Long> ids) {
        baseMapper.deleteByIds(ids);
    }

    @Override
    public void export(AppQuery query, HttpServletResponse response) {
        List<SysApp> entityList = baseMapper.selectList(QueryWrapperBuilder.build(query,SysApp.class));
        List<AppDetailResp> list = appAssembler.toDetailList(entityList);
        ExcelUtils.export(list, "导出数据", AppDetailResp.class, response);
    }
}