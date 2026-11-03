
package top.wyhao.mybatis.datapermission.handler;

import com.baomidou.mybatisplus.extension.plugins.handler.MultiDataPermissionHandler;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.schema.Table;
import top.wyhao.mybatis.datapermission.annotation.DataScope;
import top.wyhao.cmn.identity.LoginUser;
import top.wyhao.satoken.util.LoginUtil;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 默认数据权限处理器
 *
 */
@Slf4j
public class DefaultDataPermissionHandler implements MultiDataPermissionHandler {
    /**
     * 缓存 mappedStatementId -> DataScope注解,避免每次反射解析
     */
    private static final Map<String, DataScope> ANNOTATION_CACHE = new ConcurrentHashMap<>();

    @Override
    public Expression getSqlSegment(final Table table, final Expression where, final String mappedStatementId) {
        DataScope dataPermission = getAnnotation(mappedStatementId);
        if (dataPermission == null) {
            return null; // 该方法没标注解,不做数据权限过滤
        }
        LoginUser user = LoginUtil.getLoginUser();
        // 未登录或超管:不加任何限制
        if (user == null || LoginUtil.isSuperadmin()) {
            return null;
        }


        return DataPermissionPredicate.build(
                user.getRoleDataScopes(),
                user.getUserId(),
                dataPermission.userAlias(),
                dataPermission.userIdColumn(),
                dataPermission.departmentAlias(),
                dataPermission.departmentIdColumn());
    }

    /**
     * 根据 mappedStatementId(形如 com.xxx.mapper.SysUserMapper.selectUserList)
     * 反射拿到方法上的 @DataScope 注解
     */
    private DataScope getAnnotation(String mappedStatementId) {
        return ANNOTATION_CACHE.computeIfAbsent(mappedStatementId, id -> {
            try {
                int idx = id.lastIndexOf(".");
                String className = id.substring(0, idx);
                String methodName = id.substring(idx + 1);
                Class<?> clazz = Class.forName(className);
                for (Method method : clazz.getMethods()) {
                    if (method.getName().equals(methodName)
                            && method.isAnnotationPresent(DataScope.class)) {
                        return method.getAnnotation(DataScope.class);
                    }
                }
            } catch (Exception ignored) {}
            return null;
        });
    }
}
