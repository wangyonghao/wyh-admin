package top.wyhao.system.admin.infrastructure.persistence;

import cn.hutool.core.bean.BeanUtil;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Domain ↔ DO 浅拷贝（字段同名）。
 */
public final class AdminDoMapper {

    private AdminDoMapper() {
    }

    public static <D> D toDo(Object domain, Class<D> doType) {
        if (domain == null) {
            return null;
        }
        return BeanUtil.copyProperties(domain, doType);
    }

    public static <T> T toDomain(Object dataObject, Class<T> domainType) {
        if (dataObject == null) {
            return null;
        }
        return BeanUtil.copyProperties(dataObject, domainType);
    }

    public static <D> List<D> toDoList(List<?> domains, Class<D> doType) {
        if (domains == null || domains.isEmpty()) {
            return Collections.emptyList();
        }
        return domains.stream().map(d -> toDo(d, doType)).collect(Collectors.toList());
    }

    public static <T> List<T> toDomainList(List<?> dataObjects, Class<T> domainType) {
        if (dataObjects == null || dataObjects.isEmpty()) {
            return Collections.emptyList();
        }
        return dataObjects.stream().map(d -> toDomain(d, domainType)).collect(Collectors.toList());
    }

    /**
     * insert/update 后把 DO 生成的主键等字段写回领域对象。
     */
    public static void copyBack(Object dataObject, Object domain) {
        if (dataObject == null || domain == null) {
            return;
        }
        BeanUtil.copyProperties(dataObject, domain);
    }
}
