package top.wyhao.system.admin.app.service.impl;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import me.ahoo.cosid.provider.DefaultIdGeneratorProvider;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import top.wyhao.cmn.identity.LoginUser;
import top.wyhao.satoken.util.LoginUtil;
import top.wyhao.system.admin.infrastructure.persistence.dataobject.CatalogAuditDO;
import top.wyhao.system.admin.infrastructure.persistence.mapper.CatalogAuditMapper;

import java.time.LocalDateTime;

/**
 * 菜单和套餐变更的追加审计。与业务写在同一个事务里。
 */
@Component
@RequiredArgsConstructor
public class CatalogAuditWriter {

    private final CatalogAuditMapper catalogAuditMapper;

    public void record(String module, String action, String objectId, String objectName, String before, String after) {
        CatalogAuditDO row = new CatalogAuditDO();
        row.setId(DefaultIdGeneratorProvider.INSTANCE.getShare().generate());
        row.setModule(module);
        row.setAction(action);
        row.setObjectId(objectId);
        row.setObjectName(truncate(objectName, 100));
        row.setBeforeValue(before);
        row.setAfterValue(after);
        row.setResult("SUCCESS");
        row.setCreateTime(LocalDateTime.now());
        LoginUser user = LoginUtil.getLoginUser();
        if (user != null) {
            row.setOperatorId(user.getUserId());
            row.setOperatorName(user.getUsername());
        }
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            row.setIp(request.getRemoteAddr());
            row.setUserAgent(truncate(request.getHeader("User-Agent"), 200));
        }
        catalogAuditMapper.insert(row);
    }

    private static String truncate(String value, int max) {
        if (value == null || value.length() <= max) {
            return value;
        }
        return value.substring(0, max);
    }
}
