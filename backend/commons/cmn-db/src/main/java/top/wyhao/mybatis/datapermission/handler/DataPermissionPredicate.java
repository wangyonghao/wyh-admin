package top.wyhao.mybatis.datapermission.handler;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.operators.relational.InExpression;
import net.sf.jsqlparser.expression.operators.relational.ParenthesedExpressionList;
import net.sf.jsqlparser.schema.Column;
import top.wyhao.cmn.core.enums.DataScopeEnum;
import top.wyhao.cmn.core.model.RoleDataScope;

import java.util.ArrayList;
import java.util.List;

/**
 * 把登录用户的角色数据范围收成一行 SQL 条件。
 *
 * <p>任一角色是全部数据时不加限制。仅本人只比较用户列，不连带部门。
 * 部门范围没有可见部门时，这一支不产生条件；所有角色都落空则拒绝全部行。
 */
public final class DataPermissionPredicate {

    private DataPermissionPredicate() {
    }

    public static Expression build(List<RoleDataScope> roles,
                                   Long userId,
                                   String userAlias,
                                   String userIdColumn,
                                   String departmentAlias,
                                   String departmentIdColumn) {
        if (roles == null || roles.isEmpty()) {
            return denyAll();
        }
        List<Expression> orConditions = new ArrayList<>();
        for (RoleDataScope role : roles) {
            DataScopeEnum dataScope = role.getDataScope();
            if (dataScope == null) {
                continue;
            }
            switch (dataScope) {
                case ALL -> {
                    return null;
                }
                case SELF -> {
                    if (userId != null) {
                        orConditions.add(eq(userAlias, userIdColumn, userId));
                    }
                }
                case DEPARTMENT, DEPARTMENT_AND_CHILD, CUSTOM_DEPARTMENT -> {
                    Expression departments = in(departmentAlias, departmentIdColumn, role.getVisibleDepartmentIds());
                    if (departments != null) {
                        orConditions.add(departments);
                    }
                }
                default -> {
                }
            }
        }
        if (orConditions.isEmpty()) {
            return denyAll();
        }
        Expression combined = orConditions.get(0);
        for (int i = 1; i < orConditions.size(); i++) {
            combined = new OrExpression(combined, orConditions.get(i));
        }
        return new ParenthesedExpressionList<>(combined);
    }

    private static Expression denyAll() {
        return new EqualsTo(new LongValue(1), new LongValue(0));
    }

    private static Expression eq(String alias, String column, Long value) {
        return new EqualsTo(column(alias, column), new LongValue(value));
    }

    private static Expression in(String alias, String column, List<Long> values) {
        if (values == null || values.isEmpty()) {
            return null;
        }
        ExpressionList<Expression> list = new ExpressionList<>();
        values.forEach(value -> list.addExpressions(new LongValue(value)));
        InExpression expression = new InExpression();
        expression.setLeftExpression(column(alias, column));
        expression.setRightExpression(list);
        return expression;
    }

    private static Column column(String alias, String column) {
        return new Column(StringUtils.isBlank(alias) ? column : alias + "." + column);
    }
}
