package top.wyhao.mybatis.query;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.Data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 统一分页响应体，避免把 MyBatis-Plus 的 Page 直接暴露给前端。
 * <p>统一字段：{@code records} / {@code total} / {@code page} / {@code pageSize} / {@code pages}</p>
 */
@Data
public class PageResult<T> {

    /** 列表数据 */
    private List<T> records = Collections.emptyList();
    private long total;
    private long page;
    private long pageSize;
    private long pages;

    public static <T> PageResult<T> of(IPage<T> pageData) {
        PageResult<T> r = new PageResult<>();
        r.setRecords(pageData.getRecords());
        r.setTotal(pageData.getTotal());
        r.setPage(pageData.getCurrent());
        r.setPageSize(pageData.getSize());
        r.setPages(pageData.getPages());
        return r;
    }

    public static <T> PageResult<T> empty(PageParam query) {
        PageResult<T> r = new PageResult<>();
        r.setPage(query.getPage());
        r.setPageSize(query.getPageSize());
        return r;
    }

    /**
     * 对已在内存中的全量列表做分页。
     */
    public static <T> PageResult<T> of(long page, long pageSize, List<T> list) {
        PageResult<T> r = new PageResult<>();
        r.setPage(page);
        r.setPageSize(pageSize);
        if (list == null || list.isEmpty()) {
            r.setRecords(Collections.emptyList());
            r.setTotal(0);
            r.setPages(0);
            return r;
        }
        long total = list.size();
        long fromIndex = (page - 1) * pageSize;
        List<T> pageList;
        if (fromIndex < 0 || fromIndex >= list.size()) {
            pageList = new ArrayList<>(0);
        } else {
            int toIndex = (int) Math.min(fromIndex + pageSize, list.size());
            pageList = list.subList((int) fromIndex, toIndex);
        }
        r.setRecords(pageList);
        r.setTotal(total);
        r.setPages(pageSize <= 0 ? 0 : (total + pageSize - 1) / pageSize);
        return r;
    }

    /**
     * 实体 -> VO 转换，省去每个 Service 手动拷贝分页元信息
     */
    public <R> PageResult<R> map(Function<T, R> converter) {
        List<T> source = records == null ? Collections.emptyList() : records;
        PageResult<R> r = new PageResult<>();
        r.setRecords(source.stream().map(converter).collect(Collectors.toList()));
        r.setTotal(total);
        r.setPage(page);
        r.setPageSize(pageSize);
        r.setPages(pages);
        return r;
    }
}
