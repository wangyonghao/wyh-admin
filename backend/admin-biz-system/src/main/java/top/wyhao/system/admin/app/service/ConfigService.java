package top.wyhao.system.admin.app.service;

import jakarta.servlet.http.HttpServletResponse;
import top.wyhao.settings.client.MailConfigVO;
import top.wyhao.mybatis.query.PageResult;
import top.wyhao.system.admin.app.dto.ConfigQuery;
import top.wyhao.system.admin.app.dto.ConfigRequest;
import top.wyhao.system.admin.app.dto.config.RegisterConfigVO;
import top.wyhao.system.admin.app.dto.config.StorageConfigVO;
import top.wyhao.system.admin.app.dto.ConfigResult;
import top.wyhao.web.core.model.PageQuery;

import java.util.List;

/**
 * 系统配置业务（通用 CRUD + settings 自有配置项）。
 * <p>
 * 跨域配置（站点/登录/密码策略/短信）由各业务域通过 {@code ConfigStoreApi} 自行实现，不在此扩展。
 */
public interface ConfigService {

    PageResult<ConfigResult> page(ConfigQuery query, PageQuery pageQuery);

    ConfigResult detail(Long id);

    ConfigResult getByKey(String configKey);

    RegisterConfigVO getRegisterConfig();

    void updateRegisterConfig(RegisterConfigVO config);

    MailConfigVO getMailConfig();

    void updateMailConfig(MailConfigVO config);

    StorageConfigVO getStorageConfig();

    void updateStorageConfig(StorageConfigVO config);

    Long create(ConfigRequest request);

    void update(Long id, ConfigRequest request);

    void updateByKey(String configKey, ConfigRequest request);

    void delete(List<Long> ids);

    void export(ConfigQuery query, HttpServletResponse response);
}
