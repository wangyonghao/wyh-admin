
package top.wyhao.admin.generator.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.file.FileNameUtil;
import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.ZipUtil;
import cn.hutool.db.meta.Column;
import cn.hutool.db.meta.Table;
import cn.hutool.extra.template.TemplateConfig;
import cn.hutool.extra.template.TemplateEngine;
import cn.hutool.extra.template.TemplateUtil;
import cn.hutool.extra.template.engine.freemarker.FreemarkerEngine;
import cn.hutool.system.SystemUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.DefaultObjectWrapperBuilder;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.wyhao.admin.generator.config.properties.GeneratorProperties;
import top.wyhao.admin.generator.enums.FormTypeEnum;
import top.wyhao.admin.generator.enums.QueryTypeEnum;
import top.wyhao.admin.generator.mapper.GenGenConfigMapper;
import top.wyhao.admin.generator.mapper.GenGenFieldConfigMapper;
import top.wyhao.admin.generator.model.entity.GenConfig;
import top.wyhao.admin.generator.model.entity.GenFieldConfig;
import top.wyhao.admin.generator.model.entity.InnerGenConfig;
import top.wyhao.admin.generator.model.query.GenConfigQuery;
import top.wyhao.admin.generator.model.req.GenConfigReq;
import top.wyhao.admin.generator.model.resp.GeneratePreviewResp;
import top.wyhao.admin.generator.service.GeneratorService;
import top.wyhao.cmn.db.dialect.DatabaseType;
import top.wyhao.cmn.db.util.DBMetaUtils;
import top.wyhao.starter.core.autoconfigure.application.ApplicationProperties;
import top.wyhao.starter.core.constant.StringConstants;
import top.wyhao.starter.core.enums.BaseEnum;
import top.wyhao.starter.core.exception.SystemException;
import top.wyhao.starter.core.util.CollUtils;
import top.wyhao.starter.core.util.validation.BizAssert;
import top.wyhao.starter.web.core.model.PageQuery;
import top.wyhao.starter.web.core.model.PageResult;
import top.wyhao.starter.web.util.HttpUtil;

import javax.sql.DataSource;
import java.io.File;
import java.io.FileInputStream;
import java.sql.SQLException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 代码生成业务实现

 * @since 2023/4/12 23:58
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GeneratorServiceImpl implements GeneratorService {

    private final DataSource dataSource;
    private final GeneratorProperties generatorProperties;
    private final ApplicationProperties applicationProperties;
    private final GenGenFieldConfigMapper fieldConfigMapper;
    private final GenGenConfigMapper genConfigMapper;
    private static final List<String> TIME_PACKAGE_CLASS = Arrays.asList("LocalDate", "LocalTime", "LocalDateTime");

    @Override
    public PageResult<GenConfig> pageGenConfig(GenConfigQuery query, PageQuery pageQuery) {
        // 查询所有表
        List<Table> tableList = DBMetaUtils.getTables(dataSource);
        tableList.removeIf(table -> StrUtil.equalsAnyIgnoreCase(table.getTableName(), generatorProperties
            .getExcludeTables()));
        String tableName = query.getTableName();
        if (StrUtil.isNotBlank(tableName)) {
            tableList.removeIf(table -> !StrUtil.containsAnyIgnoreCase(table.getTableName(), tableName));
        }
        // 查询生成配置
        List<GenConfig> list = tableList.parallelStream().map(table -> {
            GenConfig genConfig = genConfigMapper.selectById(table.getTableName());
            if (genConfig == null) {
                genConfig = new GenConfig(table.getTableName());
            }
            genConfig.setComment(table.getComment());
            return genConfig;
        })
            .sorted(Comparator.comparing(GenConfig::getTableName)
                .thenComparing(GenConfig::getUpdateTime, Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(GenConfig::getCreateTime, Comparator.nullsLast(Comparator.naturalOrder())))
            .toList();
        // 分页
        return PageResult.build(pageQuery.getPage(), pageQuery.getSize(), list);
    }

    @Override
    public GenConfig getGenConfig(String tableName) throws SQLException {
        GenConfig genConfig = genConfigMapper.selectById(tableName);
        if (genConfig == null) {
            genConfig = new GenConfig(tableName);
            // 默认包名（当前包名）
            String packageName = ClassUtil.getPackage(GeneratorService.class);
            genConfig.setPackageName(StrUtil.subBefore(packageName, StringConstants.DOT, true));
            // 默认业务名（表注释）
            List<Table> tableList = DBMetaUtils.getTables(dataSource, tableName);
            if (CollUtil.isNotEmpty(tableList)) {
                Table table = tableList.get(0);
                genConfig.setBusinessName(StrUtil.replace(table.getComment(), "表", StringConstants.EMPTY));
            }
            // 默认作者名称（上次保存使用的作者名称）
            GenConfig lastGenConfig = genConfigMapper.selectOne(Wrappers.lambdaQuery(GenConfig.class)
                .orderByDesc(GenConfig::getCreateTime)
                .last("LIMIT 1"));
            if (lastGenConfig != null) {
                genConfig.setAuthor(lastGenConfig.getAuthor());
            }
        }
        return genConfig;
    }

    @Override
    public List<GenFieldConfig> listFieldConfig(String tableName, Boolean requireSync) {
        List<GenFieldConfig> fieldConfigList = fieldConfigMapper.selectListByTableName(tableName);
        if (CollUtil.isNotEmpty(fieldConfigList) && Boolean.FALSE.equals(requireSync)) {
            return fieldConfigList;
        }
        List<GenFieldConfig> latestFieldConfigList = new ArrayList<>();
        // 获取最新数据表列信息
        Collection<Column> columnList = DBMetaUtils.getColumns(dataSource, tableName);
        // 获取数据库对应的类型映射配置
        DatabaseType databaseType = DBMetaUtils.getDatabaseType(dataSource);
        Map<String, List<String>> typeMappingMap = generatorProperties.getTypeMappings().get(databaseType);
        BizAssert.throwIfEmpty(typeMappingMap, "请先配置对应数据库的类型映射");
        Set<Map.Entry<String, List<String>>> typeMappingEntrySet = typeMappingMap.entrySet();
        // 新增或更新字段配置
        Map<String, GenFieldConfig> fieldConfigMap = fieldConfigList.stream()
            .collect(Collectors.toMap(GenFieldConfig::getColumnName, Function.identity(), (key1, key2) -> key2));
        int i = 1;
        for (Column column : columnList) {
            GenFieldConfig fieldConfig = Optional.ofNullable(fieldConfigMap.get(column.getName()))
                .orElseGet(() -> new GenFieldConfig(column));
            // 更新已有字段配置
            if (fieldConfig.getCreateTime() != null) {
                fieldConfig.setColumnType(column.getTypeName());
                fieldConfig.setColumnSize(column.getSize());
            }
            String fieldType = typeMappingEntrySet.stream()
                .filter(entry -> entry.getValue().contains(fieldConfig.getColumnType()))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(null);
            fieldConfig.setFieldType(fieldType);
            fieldConfig.setFieldSort(i++);
            latestFieldConfigList.add(fieldConfig);
        }
        return latestFieldConfigList;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveConfig(GenConfigReq req, String tableName) {
        // 保存字段配置（先删除再保存）
        fieldConfigMapper.delete(Wrappers.lambdaQuery(GenFieldConfig.class).eq(GenFieldConfig::getTableName, tableName));
        List<GenFieldConfig> fieldConfigList = req.getFieldConfigs();
        for (int i = 0; i < fieldConfigList.size(); i++) {
            GenFieldConfig fieldConfig = fieldConfigList.get(i);
            // 重新设置排序
            fieldConfig.setFieldSort(i + 1);
            if (Boolean.TRUE.equals(fieldConfig.getShowInForm())) {
                fieldConfig.setFormType(ObjectUtil.defaultIfNull(fieldConfig.getFormType(), FormTypeEnum.INPUT));
            } else {
                // 在表单中不显示，不需要设置必填
                fieldConfig.setIsRequired(false);
            }
            if (Boolean.TRUE.equals(fieldConfig.getShowInQuery())) {
                fieldConfig.setFormType(ObjectUtil.defaultIfNull(fieldConfig.getFormType(), FormTypeEnum.INPUT));
                fieldConfig.setQueryType(ObjectUtil.defaultIfNull(fieldConfig.getQueryType(), QueryTypeEnum.EQ));
            } else {
                // 在查询中不显示，不需要设置查询方式
                fieldConfig.setQueryType(null);
            }
            // 既不在表单也不在查询中显示，不需要设置表单类型
            if (Boolean.FALSE.equals(fieldConfig.getShowInForm()) && Boolean.FALSE.equals(fieldConfig
                .getShowInQuery())) {
                fieldConfig.setFormType(null);
            }
            fieldConfig.setTableName(tableName);
        }
        fieldConfigMapper.insert(fieldConfigList);
        // 保存或更新生成配置信息
        GenConfig newGenConfig = req.getGenConfig();
        GenConfig oldGenConfig = genConfigMapper.selectById(tableName);
        if (oldGenConfig != null) {
            BeanUtil.copyProperties(newGenConfig, oldGenConfig);
            genConfigMapper.updateById(oldGenConfig);
        } else {
            genConfigMapper.insert(newGenConfig);
        }
    }

    @Override
    public List<GeneratePreviewResp> preview(List<String> tableNames) {
        List<GeneratePreviewResp> generatePreviewList = new ArrayList<>();
        for (String tableName : tableNames) {
            generatePreviewList.addAll(this.preview(tableName));
        }
        return generatePreviewList;
    }

    @Override
    public void downloadCode(List<String> tableNames, HttpServletResponse response) {
        try {
            String tempDir = SystemUtil.getUserInfo().getTempDir();
            // 删除旧代码
            FileUtil.del(tempDir + applicationProperties.getId());
            tableNames.forEach(tableName -> {
                // 初始化配置及数据
                List<GeneratePreviewResp> generatePreviewList = this.preview(tableName);
                // 生成代码
                this.generateCode(generatePreviewList, genConfigMapper.selectById(tableName));
            });
            // 打包下载
            File tempDirFile = new File(tempDir, applicationProperties.getId());
            String zipFilePath = tempDirFile.getPath() + jodd.io.ZipUtil.ZIP_EXT;
            ZipUtil.zip(tempDirFile.getPath(), zipFilePath);
            File zipFile =  new File(zipFilePath);
            HttpUtil.writeAttachmentToResponse(new FileInputStream(zipFile), zipFile.getName(),response);
        } catch (Exception e) {
            log.error("Generate code of table '{}' occurred an error. {}", tableNames, e.getMessage(), e);
            throw new SystemException("代码生成失败，请手动清理生成文件");
        }
    }

    @Override
    public void generateCode(List<String> tableNames) {
        try {
            String projectPath = System.getProperty("user.dir");
            tableNames.forEach(tableName -> {
                // 初始化配置及数据
                List<GeneratePreviewResp> generatePreviewList = this.preview(tableName);
                // 生成代码
                for (GeneratePreviewResp generatePreview : generatePreviewList) {
                    // 后端：tide-admin/continew-system/src/main/java/top.wyhao/system/service/impl/XxxServiceImpl.java
                    // 前端：tide-admin/tide-admin-ui/src/views/system/user/index.vue
                    File file = new File(projectPath + generatePreview.getPath()
                        .replace("tide-admin\\tide-admin", ""), generatePreview.getFileName());
                    // 如果已经存在，且不允许覆盖，则跳过
                    if (!file.exists() || Boolean.TRUE.equals(genConfigMapper.selectById(tableName).getIsOverride())) {
                        FileUtil.writeUtf8String(generatePreview.getContent(), file);
                    }
                }
            });

        } catch (Exception e) {
            log.error("Generate code of table '{}' occurred an error. {}", tableNames, e.getMessage(), e);
            throw new SystemException("代码生成失败，请手动清理生成文件");
        }
    }

    /**
     * 生成预览
     *
     * @param tableName 表名称
     * @return 预览信息
     */
    private List<GeneratePreviewResp> preview(String tableName) {
        List<GeneratePreviewResp> generatePreviewList = new ArrayList<>();
        // 初始化配置
        GenConfig genConfig = genConfigMapper.selectById(tableName);
        BizAssert.isNull(genConfig, "请先进行数据表 [{}] 生成配置", tableName);
        List<GenFieldConfig> fieldConfigList = fieldConfigMapper.selectListByTableName(tableName);
        BizAssert.throwIfEmpty(fieldConfigList, "请先进行数据表 [{}] 字段配置", tableName);

        InnerGenConfig innerGenConfig = new InnerGenConfig(genConfig);
        List<String> imports = new ArrayList<>();
        // 处理枚举字段
        List<GenFieldConfig> fieldConfigRecords = CollUtils
            .mapToList(fieldConfigList, s -> convertToFieldConfig(s, imports));
        innerGenConfig.setImports(imports);

        // 渲染代码
        String classNamePrefix = innerGenConfig.getClassNamePrefix();
        Map<String, GeneratorProperties.TemplateConfig> templateConfigMap = generatorProperties.getTemplateConfigs();
        TemplateEngine engine = TemplateUtil
            .createEngine(new TemplateConfig("templates", TemplateConfig.ResourceMode.CLASSPATH));
        // 在模板中允许使用静态方法
        if (engine instanceof FreemarkerEngine freemarkerEngine) {
            DefaultObjectWrapper wrapper = new DefaultObjectWrapperBuilder(Configuration.VERSION_2_3_33).build();
            freemarkerEngine.getConfiguration().setSharedVariable("statics", wrapper.getStaticModels());
        }
        for (Map.Entry<String, GeneratorProperties.TemplateConfig> templateConfigEntry : templateConfigMap.entrySet()) {
            GeneratorProperties.TemplateConfig templateConfig = templateConfigEntry.getValue();
            // 移除需要忽略的字段
            innerGenConfig.setFieldConfigs(fieldConfigRecords.stream()
                .filter(fieldConfig -> !StrUtil.equalsAny(fieldConfig.getFieldName(), templateConfig
                    .getExcludeFields()))
                .toList());
            // 预处理配置
            this.pretreatment(innerGenConfig);
            // 处理其他配置
            innerGenConfig.setSubPackageName(templateConfig.getPackageName());
            String classNameSuffix = templateConfigEntry.getKey();
            String className = classNamePrefix + StrUtil.blankToDefault(templateConfig.getSuffix(), classNameSuffix);
            innerGenConfig.setClassName(className);
            boolean isBackend = templateConfig.isBackend();
            String extension = templateConfig.getExtension();
            GeneratePreviewResp generatePreview = new GeneratePreviewResp();
            generatePreview.setBackend(isBackend);
            generatePreviewList.add(generatePreview);
            String fileName = className + extension;
            if (!isBackend) {
                fileName = ".vue".equals(extension) && "index".equals(classNameSuffix)
                    ? "index.vue"
                    : this.getFrontendFileName(classNamePrefix, classNameSuffix, extension);
            }
            generatePreview.setFileName(fileName);
            generatePreview.setContent(engine.getTemplate(templateConfig.getTemplatePath())
                .render(BeanUtil.beanToMap(innerGenConfig)));
            this.setPreviewPath(generatePreview, innerGenConfig, templateConfig);
        }
        return generatePreviewList;
    }

    /**
     * 添加枚举类型的属性，生成对应的import
     *
     * @param fieldConfig 属性配置信息
     * @param imports       待导入包集合
     * @return 新的属性配置信息
     */
    private GenFieldConfig convertToFieldConfig(GenFieldConfig fieldConfig, List<String> imports) {
        GenFieldConfig result = new GenFieldConfig();
        BeanUtil.copyProperties(fieldConfig, result);
        String dictCode = result.getDictCode();
        if (StringUtils.isBlank(dictCode)) {
            return result;
        }
        Set<Class<?>> classSet = ClassUtil.scanPackageBySuper(applicationProperties.getBasePackage(), BaseEnum.class);
        Optional<Class<?>> clazzOptional = classSet.stream()
            .filter(s -> StrUtil.toUnderlineCase(s.getSimpleName()).toLowerCase().equals(dictCode))
            .findFirst();
        if (clazzOptional.isEmpty()) {
            return result;
        }
        Class<?> clazz = clazzOptional.get();
        imports.add(clazz.getName());
        result.setFieldType(clazz.getSimpleName());
        return result;
    }

    /**
     * 设置预览路径
     *
     * @param generatePreview 预览信息
     * @param genConfig       生成配置
     * @param templateConfig  模板配置
     */
    private void setPreviewPath(GeneratePreviewResp generatePreview,
                                InnerGenConfig genConfig,
                                GeneratorProperties.TemplateConfig templateConfig) {
        // 获取前后端基础路径
        String backendBasicPackagePath = this.buildBackendBasicPackagePath(genConfig, templateConfig);
        String frontendBasicPackagePath = String.join(File.separator, applicationProperties
            .getId(), applicationProperties.getId() + "-ui");
        String packagePath;
        if (generatePreview.isBackend()) {
            // 例如：tide-admin/continew-system/src/main/java/top.wyhao/system/service/impl
            packagePath = String.join(File.separator, backendBasicPackagePath, templateConfig.getPackageName()
                .replace(StringConstants.DOT, File.separator));
        } else {
            // 例如：tide-admin/tide-admin-ui/src/views/system
            packagePath = String.join(File.separator, frontendBasicPackagePath, templateConfig.getPackageName()
                .replace(StringConstants.SLASH, File.separator), genConfig.getApiModuleName());
            // 例如：tide-admin/tide-admin-ui/src/views/system/user
            packagePath = ".vue".equals(templateConfig.getExtension())
                ? packagePath + File.separator + StrUtil.lowerFirst(genConfig.getClassNamePrefix())
                : packagePath;
        }
        generatePreview.setPath(packagePath);
    }

    /**
     * 生成代码
     *
     * @param generatePreviewList 生成预览列表
     * @param genConfig           生成配置
     */
    private void generateCode(List<GeneratePreviewResp> generatePreviewList, GenConfig genConfig) {
        for (GeneratePreviewResp generatePreview : generatePreviewList) {
            // 后端：tide-admin/continew-system/src/main/java/top.wyhao/system/service/impl/XxxServiceImpl.java
            // 前端：tide-admin/tide-admin-ui/src/views/system/user/index.vue
            File file = new File(SystemUtil.getUserInfo().getTempDir() + generatePreview.getPath(), generatePreview
                .getFileName());
            // 如果已经存在，且不允许覆盖，则跳过
            if (!file.exists() || Boolean.TRUE.equals(genConfig.getIsOverride())) {
                FileUtil.writeUtf8String(generatePreview.getContent(), file);
            }
        }
    }

    /**
     * 构建后端包路径
     *
     * @param genConfig      生成配置
     * @param templateConfig 模板配置
     * @return 后端包路径
     */
    private String buildBackendBasicPackagePath(GenConfig genConfig,
                                                GeneratorProperties.TemplateConfig templateConfig) {
        String extension = templateConfig.getExtension();
        // 例如：tide-admin/continew-system/src/main/java/top.wyhao/system
        return String.join(File.separator, applicationProperties.getId(), applicationProperties.getId(), genConfig
            .getModuleName(), "src", "main", FileNameUtil.EXT_JAVA.equals(extension)
                ? "java"
                : "resources") + (FileNameUtil.EXT_JAVA.equals(extension)
                    ? File.separator + genConfig.getPackageName().replace(StringConstants.DOT, File.separator)
                    : StringConstants.EMPTY);
    }

    /**
     * 获取前端文件名
     *
     * @param classNamePrefix 类名前缀
     * @param className       类名
     * @param extension       扩展名
     * @return 前端文件名
     */
    private String getFrontendFileName(String classNamePrefix, String className, String extension) {
        return (".ts".equals(extension) ? StrUtil.lowerFirst(classNamePrefix) : className) + extension;
    }

    /**
     * 预处理生成配置
     *
     * @param genConfig 生成配置
     */
    private void pretreatment(InnerGenConfig genConfig) {
        List<GenFieldConfig> fieldConfigList = genConfig.getFieldConfigs();
        // 统计部分特殊字段特征
        Set<String> dictCodeSet = new HashSet<>();
        for (GenFieldConfig fieldConfig : fieldConfigList) {
            String fieldType = fieldConfig.getFieldType();
            // 必填项
            if (Boolean.TRUE.equals(fieldConfig.getIsRequired())) {
                genConfig.setHasRequiredField(true);
            }
            // 数据类型
            if ("BigDecimal".equals(fieldType)) {
                genConfig.setHasBigDecimalField(true);
            }
            if (TIME_PACKAGE_CLASS.contains(fieldType)) {
                genConfig.setHasTimeField(true);
            }
            // 字典码
            if (StrUtil.isNotBlank(fieldConfig.getDictCode())) {
                genConfig.setHasDictField(true);
                dictCodeSet.add(fieldConfig.getDictCode());
            }
        }
        genConfig.setDictCodes(dictCodeSet);
    }
}
