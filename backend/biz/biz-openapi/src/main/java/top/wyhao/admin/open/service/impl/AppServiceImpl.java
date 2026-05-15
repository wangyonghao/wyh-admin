
package top.wyhao.admin.open.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.codec.Base64;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.ValidationException;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.wyhao.admin.open.mapper.AppMapper;
import top.wyhao.admin.open.model.entity.AppDO;
import top.wyhao.admin.open.model.query.AppQuery;
import top.wyhao.admin.open.model.req.AppReq;
import top.wyhao.admin.open.model.resp.AppDetailResp;
import top.wyhao.admin.open.model.resp.AppResp;
import top.wyhao.admin.open.model.resp.AppSecretResp;
import top.wyhao.admin.open.service.AppService;
import top.wyhao.cmn.db.model.ServiceImpl;
import top.wyhao.cmn.db.util.QueryWrapperUtil;
import top.wyhao.starter.core.constant.StringConstants;
import top.wyhao.starter.core.util.ReflectUtils;
import top.wyhao.starter.excel.util.ExcelUtils;
import top.wyhao.starter.web.core.model.PageQuery;
import top.wyhao.starter.web.core.model.PageResult;
import top.wyhao.starter.web.core.model.SortQuery;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;

/**
 * 应用业务实现
 *
 * @author chengzi
 * @author Charles7c
 * @since 2024/10/17 16:03
 */
@Service
public class AppServiceImpl extends ServiceImpl<AppMapper, AppDO> implements AppService {

    private List<Field> queryFields;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(AppReq req) {
        req.setAccessKey(Base64.encode(IdUtil.fastSimpleUUID())
                .replace(StringConstants.SLASH, StringConstants.EMPTY)
                .replace(StringConstants.PLUS, StringConstants.EMPTY)
                .substring(0, 30));
        req.setSecretKey(this.generateSecret());

        AppDO entity = BeanUtil.copyProperties(req, getEntityClass());
        baseMapper.insert(entity);
        return entity.getId();
    }


    @Override
    public AppSecretResp getSecret(Long id) {
        AppDO app = super.getById(id);
        AppSecretResp appSecretResp = new AppSecretResp();
        appSecretResp.setAccessKey(app.getAccessKey());
        appSecretResp.setSecretKey(app.getSecretKey());
        return appSecretResp;
    }

    @Override
    public void resetSecret(Long id) {
        super.getById(id);
        AppDO app = new AppDO();
        app.setSecretKey(this.generateSecret());
        baseMapper.update(app, Wrappers.lambdaQuery(AppDO.class).eq(AppDO::getId, id));
    }

    @Override
    public AppDO getByAccessKey(String accessKey) {
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
    public PageResult<AppResp> findPage(AppQuery query, PageQuery pageQuery) {
        QueryWrapper<AppDO> queryWrapper = this.buildQueryWrapper(query);
        QueryWrapperUtil.applySort(queryWrapper, query.getSort(),AppDO.class);
        IPage<AppDO> page = baseMapper.selectPage(new Page<>(pageQuery.getPage(), pageQuery.getSize()), queryWrapper);
        return PageResult.build(page, AppResp.class);
    }

    @Override
    public List<AppResp> list(AppQuery query, SortQuery sortQuery) {
        QueryWrapper<AppDO> queryWrapper = this.buildQueryWrapper(query);
        // 设置排序
        QueryWrapperUtil.applySort(queryWrapper, sortQuery.getSort(),AppDO.class);
        List<AppDO> entityList = baseMapper.selectList(queryWrapper);
        return BeanUtil.copyToList(entityList, AppResp.class);
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(AppReq req, Long id) {
        AppDO entity = this.getById(id);
        BeanUtil.copyProperties(req, entity, CopyOptions.create().ignoreNullValue());
        baseMapper.updateById(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(List<Long> ids) {
        baseMapper.deleteByIds(ids);
    }

    @Override
    public void export(AppQuery query, SortQuery sortQuery, HttpServletResponse response) {
        QueryWrapper<AppDO> queryWrapper = this.buildQueryWrapper(query);
        // 设置排序
        this.sort(queryWrapper, sortQuery);
        List<AppDO> entityList = baseMapper.selectList(queryWrapper);
        List<AppDetailResp> list = BeanUtil.copyToList(entityList, AppDetailResp.class);
        ExcelUtils.export(list, "导出数据", AppDetailResp.class, response);
    }

    public List<Field> getQueryFields() {
        if (this.queryFields == null) {
            this.queryFields = ReflectUtils.getNonStaticFields(AppQuery.class);
        }
        return queryFields;
    }

    protected void sort(QueryWrapper<AppDO> queryWrapper, SortQuery sortQuery) {
        if (sortQuery == null || sortQuery.getSort().isUnsorted()) {
            return;
        }
        Sort sort = sortQuery.getSort();
        for (Sort.Order order : sort) {
            String property = order.getProperty();
            String checkProperty;
            // 携带表别名则获取 . 后面的字段名
            if (property.contains(StringConstants.DOT)) {
                checkProperty = CollUtil.getLast(CharSequenceUtil.split(property, StringConstants.DOT));
            } else {
                checkProperty = property;
            }
            Optional<Field> optional = getEntityFields().stream()
                .filter(field -> checkProperty.equals(field.getName()))
                .findFirst();

            if(optional.isEmpty()){
                throw new ValidationException(StrUtil.format("无效的排序字段 [{}]", property));
            }
            queryWrapper.orderBy(true, order.isAscending(), CharSequenceUtil.toUnderlineCase(property));
        }
    }

    protected QueryWrapper<AppDO> buildQueryWrapper(AppQuery query) {
        QueryWrapper<AppDO> queryWrapper = new QueryWrapper<>();
        // 解析并拼接查询条件
        return QueryWrapperUtil.build(query, this.getQueryFields(), queryWrapper);
    }

}