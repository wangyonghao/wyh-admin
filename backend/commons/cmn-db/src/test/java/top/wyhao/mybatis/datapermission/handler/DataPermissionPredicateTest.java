package top.wyhao.mybatis.datapermission.handler;

import net.sf.jsqlparser.expression.Expression;
import org.junit.jupiter.api.Test;
import top.wyhao.cmn.core.enums.DataScopeEnum;
import top.wyhao.cmn.core.model.RoleDataScope;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DataPermissionPredicateTest {

    @Test
    void allDataShortCircuitsToNoFilter() {
        Expression expression = DataPermissionPredicate.build(
                List.of(role(DataScopeEnum.ALL, null), role(DataScopeEnum.SELF, List.of())),
                9L,
                "u",
                "id",
                "d",
                "dept_id");

        assertThat(expression).isNull();
    }

    @Test
    void selfComparesOnlyTheUserColumn() {
        RoleDataScope self = role(DataScopeEnum.SELF, List.of(1L, 2L));

        Expression expression = DataPermissionPredicate.build(List.of(self), 9L, "u", "id", "d", "dept_id");

        assertThat(expression).isNotNull();
        assertThat(expression.toString()).contains("u.id = 9");
        assertThat(expression.toString()).doesNotContain("dept_id");
    }

    @Test
    void emptyDepartmentScopeDoesNotFallThroughAndDeniesWhenNothingMatches() {
        RoleDataScope department = role(DataScopeEnum.DEPARTMENT, List.of());
        RoleDataScope custom = role(DataScopeEnum.CUSTOM_DEPARTMENT, null);

        Expression expression = DataPermissionPredicate.build(
                List.of(department, custom),
                9L,
                "u",
                "id",
                "d",
                "dept_id");

        assertThat(expression).isNotNull();
        assertThat(expression.toString()).isEqualTo("1 = 0");
    }

    @Test
    void departmentAndSelfAreOrCombined() {
        Expression expression = DataPermissionPredicate.build(
                List.of(role(DataScopeEnum.SELF, null), role(DataScopeEnum.DEPARTMENT_AND_CHILD, List.of(3L, 4L))),
                9L,
                "",
                "user_id",
                "t",
                "department_id");

        assertThat(expression).isNotNull();
        String sql = expression.toString();
        assertThat(sql).contains("user_id = 9");
        assertThat(sql).contains("t.department_id");
        assertThat(sql).contains("3");
        assertThat(sql).contains("4");
        assertThat(sql).containsIgnoringCase("OR");
    }

    @Test
    void noRolesDenyAllRows() {
        Expression expression = DataPermissionPredicate.build(List.of(), 9L, "u", "id", "d", "dept_id");

        assertThat(expression).isNotNull();
        assertThat(expression.toString()).isEqualTo("1 = 0");
    }

    private static RoleDataScope role(DataScopeEnum scope, List<Long> departments) {
        RoleDataScope role = new RoleDataScope();
        role.setDataScope(scope);
        role.setVisibleDepartmentIds(departments);
        return role;
    }
}
