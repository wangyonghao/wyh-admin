package top.wyhao.tenant.domain;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.NullValue;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import top.wyhao.tenant.context.TenantContext;
import top.wyhao.tenant.context.TenantContextHolder;
import top.wyhao.tenant.isolation.DefaultTenantLineHandler;
import top.wyhao.tenant.config.TenantTableRegistry;
import top.wyhao.tenant.exception.TenantException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 行级隔离与登录后租户选择：上下文里的租户写进 SQL，已登录会话不被请求头改写。
 */
class TenantIsolationTest {

    private final TenantTableRegistry registry = new TenantTableRegistry();
    private final DefaultTenantLineHandler handler = new DefaultTenantLineHandler(registry);

    @AfterEach
    void clearTenant() {
        TenantContextHolder.clear();
    }

    @Test
    void rowPredicateUsesContextTenantAndSkipsSharedTables() {
        registry.register("sys_user");
        TenantContext context = new TenantContext();
        context.setTenantId(42L);
        TenantContextHolder.setContext(context);

        Expression expression = handler.getTenantId();
        assertThat(expression).isInstanceOf(LongValue.class);
        assertThat(expression.toString()).isEqualTo("42");
        assertThat(handler.ignoreTable("sys_user")).isFalse();
        assertThat(handler.ignoreTable("sys_menu")).isTrue();
        assertThat(handler.ignoreTable("\"sys_user\"")).isFalse();
    }

    @Test
    void ignoreFlagAndMissingContextDoNotAdoptAnotherTenant() {
        registry.register("sys_user");
        TenantContextHolder.setIgnore(true);
        assertThat(handler.ignoreTable("sys_user")).isTrue();

        TenantContextHolder.clear();
        assertThat(handler.getTenantId()).isInstanceOf(NullValue.class);
    }

    @Test
    void loggedInSessionKeepsItsTenantWhenHeaderPointsElsewhere() {
        assertThat(TenantSelection.select(7L, "9", 0L)).isEqualTo(7L);
        assertThat(TenantSelection.select(0L, "9", 0L)).isEqualTo(0L);
    }

    @Test
    void anonymousRequestUsesHeaderOrDefaultTenant() {
        assertThat(TenantSelection.select(null, " 15 ", 0L)).isEqualTo(15L);
        assertThat(TenantSelection.select(null, " ", 0L)).isEqualTo(0L);
        assertThat(TenantSelection.select(null, null, 0L)).isEqualTo(0L);
        assertThatThrownBy(() -> TenantSelection.select(null, "abc", 0L))
            .isInstanceOf(TenantException.class)
            .extracting(ex -> ((TenantException) ex).getCode())
            .isEqualTo("TENANT_ID_INVALID");
    }
}
