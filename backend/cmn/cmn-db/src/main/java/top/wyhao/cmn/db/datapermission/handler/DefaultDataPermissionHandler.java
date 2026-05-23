
package top.wyhao.cmn.db.datapermission.handler;

import cn.hutool.extra.spring.SpringUtil;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.handler.DataPermissionHandler;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.relational.*;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.ParenthesedSelect;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.SelectItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.wyhao.starter.core.UserContextHolder;
import top.wyhao.starter.core.enums.DataScopeEnum;
import top.wyhao.starter.core.model.LoginUser;
import top.wyhao.starter.core.model.RoleVO;
import top.wyhao.cmn.db.datapermission.annotation.DataPermission;
import top.wyhao.cmn.db.datapermission.exception.DataPermissionException;
import top.wyhao.cmn.db.dialect.DatabaseType;
import top.wyhao.cmn.db.util.DBMetaUtils;

import javax.sql.DataSource;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 默认数据权限处理器
 *


 * @since 1.1.0
 */
public class DefaultDataPermissionHandler implements DataPermissionHandler {
    /**
     * 数据库字段：祖先节点
     */
    public static final String ANCESTORS_COLUMN = "ancestors";

    /**
     * 方法名后缀：COUNT
     */
    public static final String COUNT_METHOD_SUFFIX = "_COUNT";


    private static final Logger log = LoggerFactory.getLogger(DefaultDataPermissionHandler.class);
    /**
     * Mapper类中所有方法数据权限注解缓存
     */
    private final Map<String, Map<String, DataPermission>> annotationCache = new ConcurrentHashMap<>();

    @Override
    public Expression getSqlSegment(Expression where, String mappedStatementId) {
        try {
            DataPermission dataPermission = findDataPermissionAnnotation(mappedStatementId);
            if (dataPermission == null) {
                return where;
            }
            if (UserContextHolder.isSuperadmin()) {
                return where;
            }
            return buildDataScopeFilter(dataPermission, where);
        } catch (Exception e) {
            log.error("Data permission handler build data scope filter occurred an error: {}.", e.getMessage(), e);
        }
        return where;
    }

    /**
     * 查找数据权限注解
     *
     * @param mappedStatementId Mapper 方法 ID
     * @return 数据权限注解
     */
    private DataPermission findDataPermissionAnnotation(String mappedStatementId) {
        try {
            int lastDotIndex = mappedStatementId.lastIndexOf(".");
            if (lastDotIndex == -1) {
                return null;
            }

            String className = mappedStatementId.substring(0, lastDotIndex);
            String methodName = mappedStatementId.substring(lastDotIndex + 1);

            // 先根据类名从缓存获取，如果methodAnnotations不为空，则说明该类中的所有方法都已缓存， 只是值为null。
            Map<String, DataPermission> methodAnnotations = annotationCache.get(className);
            if (methodAnnotations != null) {
                // methodName 可能是 ** 或者 **_COUNT
                return methodAnnotations.getOrDefault(methodName, methodAnnotations.get(methodName + COUNT_METHOD_SUFFIX));
            }

            // 缓存未命中，执行反射操作
            Class<?> clazz = Class.forName(className);
            Method[] methods = clazz.getMethods();

            // 创建新的缓存映射
            Map<String, DataPermission> newMethodAnnotations = new ConcurrentHashMap<>();

            // 缓存所有带@DataPermission注解的方法
            for (Method method : methods) {
                String name = method.getName();
                DataPermission annotation = method.getAnnotation(DataPermission.class);
                if (annotation != null) {
                    newMethodAnnotations.put(name, annotation);
                }
            }
            // 存入缓存
            annotationCache.put(className, newMethodAnnotations);

            return newMethodAnnotations.get(methodName);
        } catch (ClassNotFoundException e) {
            throw DataPermissionException.methodNotFound(mappedStatementId);
        }
    }

    /**
     * 构建数据范围过滤条件
     *
     * @param dataPermission 数据权限
     * @param where          当前查询条件
     * @return 构建后查询条件
     */
    private Expression buildDataScopeFilter(DataPermission dataPermission, Expression where) {
        LoginUser userData = UserContextHolder.getCurrentUser();
        if (userData == null) {
            throw DataPermissionException.invalidUserData("User data is null or invalid");
        }

        Expression expression = null;
        Set<RoleVO> roles = new HashSet<>(); // todo by wyh 完善角色数据权限
//        Set<RoleVO> roles = userData.getRoles();

        for (RoleVO roleData : roles) {
            DataScopeEnum dataScope = roleData.getDataScope();
            if (DataScopeEnum.ALL.equals(dataScope)) {
                return where;
            }

            expression = switch (dataScope) {
                case DEPT_AND_CHILD -> buildDeptAndChildExpression(dataPermission, userData.getDeptId(), expression);
                case DEPT -> buildDeptExpression(dataPermission, userData.getDeptId(), expression);
                case SELF -> buildSelfExpression(dataPermission, userData.getUserId(), expression);
                case CUSTOM -> buildCustomExpression(dataPermission, roleData, expression);
                default -> throw DataPermissionException.unsupportedDataScope(dataScope.toString());
            };
        }

        return where != null ? new AndExpression(where, new ParenthesedExpressionList<>(expression)) : expression;
    }

    /**
     * 构建本部门及以下数据权限表达式
     *
     * <p>
     * 处理完后的 SQL 示例：<br /> select t1.* from table as t1 where t1.dept_id in (select id from sys_dept where id = xxx or
     * find_in_set(xxx, ancestors));
     * </p>
     *
     * @param dataPermission 数据权限
     * @param deptId         用户所属部门Id
     * @param expression     处理前的表达式
     * @return 处理完后的表达式
     */
    private Expression buildDeptAndChildExpression(DataPermission dataPermission, Long deptId, Expression expression) {
        ParenthesedSelect subSelect = new ParenthesedSelect();
        PlainSelect select = new PlainSelect();
        select.setSelectItems(Collections.singletonList(new SelectItem<>(new Column(dataPermission.id()))));
        select.setFromItem(new Table(dataPermission.deptTableAlias()));

        EqualsTo equalsTo = new EqualsTo();
        equalsTo.setLeftExpression(new Column(dataPermission.id()));
        equalsTo.setRightExpression(new LongValue(deptId));

        DatabaseType databaseType = DBMetaUtils.getDatabaseType(SpringUtil.getBean(DataSource.class));
        Expression inSetExpression;
        if (DatabaseType.MYSQL.getDatabase().equalsIgnoreCase(databaseType.getDatabase())) {
            Function findInSetFunction = new Function();
            findInSetFunction.setName("find_in_set");
            findInSetFunction.setParameters(new ExpressionList(new LongValue(deptId), new Column(ANCESTORS_COLUMN)));
            inSetExpression = findInSetFunction;
        } else if (DatabaseType.POSTGRE_SQL.getDatabase().equalsIgnoreCase(databaseType.getDatabase())) {
            // 构建 concat 函数
            Function concatFunction = new Function("concat");
            concatFunction.setParameters(new ExpressionList<>(new Column(ANCESTORS_COLUMN), new StringValue(",")));

            // 创建 LIKE 函数
            LikeExpression likeExpression = new LikeExpression();
            likeExpression.setLeftExpression(concatFunction);
            likeExpression.setRightExpression(new StringValue("%," + deptId + ",%"));
            inSetExpression = likeExpression;
        } else {
            throw DataPermissionException.unsupportedDatabase(databaseType.getDatabase());
        }

        select.setWhere(new OrExpression(equalsTo, inSetExpression));
        subSelect.setSelect(select);
        // 构建父查询
        InExpression inExpression = new InExpression();
        inExpression.setLeftExpression(this.buildColumn(dataPermission.tableAlias(), dataPermission.deptId()));
        inExpression.setRightExpression(subSelect);
        return expression != null ? new OrExpression(expression, inExpression) : inExpression;
    }

    /**
     * 构建本部门数据权限表达式
     *
     * <p>
     * 处理完后的 SQL 示例：<br /> select t1.* from table as t1 where t1.dept_id = xxx;
     * </p>
     *
     * @param dataPermission 数据权限
     * @param deptId         用户所属部门Id
     * @param expression     处理前的表达式
     * @return 处理完后的表达式
     */
    private Expression buildDeptExpression(DataPermission dataPermission, Long deptId, Expression expression) {
        EqualsTo equalsTo = new EqualsTo();
        equalsTo.setLeftExpression(this.buildColumn(dataPermission.tableAlias(), dataPermission.deptId()));
        equalsTo.setRightExpression(new LongValue(deptId));
        return expression != null ? new OrExpression(expression, equalsTo) : equalsTo;
    }

    /**
     * 构建仅本人数据权限表达式
     *
     * <p>
     * 处理完后的 SQL 示例：<br /> select t1.* from table as t1 where t1.create_user = xxx;
     * </p>
     *
     * @param dataPermission 数据权限
     * @param userId         用户Id
     * @param expression     处理前的表达式
     * @return 处理完后的表达式
     */
    private Expression buildSelfExpression(DataPermission dataPermission, Long userId, Expression expression) {
        EqualsTo equalsTo = new EqualsTo();
        equalsTo.setLeftExpression(this.buildColumn(dataPermission.tableAlias(), dataPermission.userId()));
        equalsTo.setRightExpression(new LongValue(userId));
        return expression != null ? new OrExpression(expression, equalsTo) : equalsTo;
    }

    /**
     * 构建自定义数据权限表达式
     *
     * <p>
     * 处理完后的 SQL 示例：<br /> select t1.* from table as t1 where t1.dept_id in (select dept_id from sys_role_dept where
     * role_id = xxx);
     * </p>
     *
     * @param dataPermission 数据权限
     * @param roleData       角色上下文
     * @param expression     处理前的表达式
     * @return 处理完后的表达式
     */
    private Expression buildCustomExpression(DataPermission dataPermission, RoleVO roleData, Expression expression) {
        ParenthesedSelect subSelect = new ParenthesedSelect();
        PlainSelect select = new PlainSelect();
        select.setSelectItems(Collections.singletonList(new SelectItem<>(new Column(dataPermission.deptId()))));
        select.setFromItem(new Table(dataPermission.roleDeptTableAlias()));
        EqualsTo equalsTo = new EqualsTo();
        equalsTo.setLeftExpression(new Column(dataPermission.roleId()));
        equalsTo.setRightExpression(new LongValue(roleData.getId()));
        select.setWhere(equalsTo);
        subSelect.setSelect(select);
        // 构建父查询
        InExpression inExpression = new InExpression();
        inExpression.setLeftExpression(this.buildColumn(dataPermission.tableAlias(), dataPermission.deptId()));
        inExpression.setRightExpression(subSelect);
        return expression != null ? new OrExpression(expression, inExpression) : inExpression;
    }

    /**
     * 构建 Column
     *
     * @param tableAlias 表别名
     * @param columnName 字段名称
     * @return 带表别名字段
     */
    private Column buildColumn(String tableAlias, String columnName) {
        if (StringUtils.isNotEmpty(tableAlias)) {
            return new Column("%s.%s".formatted(tableAlias, columnName));
        }
        return new Column(columnName);
    }
}
