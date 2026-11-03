package top.wyhao.system.admin.security.domain;

import org.junit.jupiter.api.Test;
import top.wyhao.system.admin.domain.model.RoleDataScopeRules;
import top.wyhao.cmn.core.enums.DataScopeEnum;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RoleDataScopeRulesTest {

    @Test
    void allAndSelfDoNotNarrowByDepartment() {
        assertThat(RoleDataScopeRules.visibleDepartmentIds(null, 1L, List.of(1L), List.of(2L))).isNull();
        assertThat(RoleDataScopeRules.visibleDepartmentIds(DataScopeEnum.ALL, 1L, List.of(1L), List.of(2L))).isNull();
        assertThat(RoleDataScopeRules.visibleDepartmentIds(DataScopeEnum.SELF, 1L, List.of(1L), List.of(2L))).isNull();
    }

    @Test
    void departmentScopesReturnConcreteIdsOrEmpty() {
        assertThat(RoleDataScopeRules.visibleDepartmentIds(DataScopeEnum.DEPARTMENT, null, null, null)).isEmpty();
        assertThat(RoleDataScopeRules.visibleDepartmentIds(DataScopeEnum.DEPARTMENT, 8L, null, null))
                .containsExactly(8L);
        assertThat(RoleDataScopeRules.visibleDepartmentIds(DataScopeEnum.DEPARTMENT_AND_CHILD, 8L, null, null))
                .isEmpty();
        assertThat(RoleDataScopeRules.visibleDepartmentIds(
                DataScopeEnum.DEPARTMENT_AND_CHILD, 8L, List.of(8L, 9L), null)).containsExactly(8L, 9L);
    }

    @Test
    void customDepartmentPassesThroughIncludingNull() {
        assertThat(RoleDataScopeRules.visibleDepartmentIds(DataScopeEnum.CUSTOM_DEPARTMENT, 1L, null, null)).isNull();
        assertThat(RoleDataScopeRules.visibleDepartmentIds(
                DataScopeEnum.CUSTOM_DEPARTMENT, 1L, null, List.of(3L, 4L))).containsExactly(3L, 4L);
    }
}
