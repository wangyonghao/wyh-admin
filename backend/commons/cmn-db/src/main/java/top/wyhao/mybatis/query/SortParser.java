package top.wyhao.mybatis.query;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * 解析前端传来的排序串，支持多字段。
 *
 * <p>支持的写法（可混用，逗号分隔）：</p>
 * <pre>
 *   orderBy=createTime                       单字段，方向取 orderDir
 *   orderBy=status,createTime                多字段，方向都取 orderDir
 *   orderBy=status asc,createTime desc       每项单独指定方向（空格）
 *   orderBy=status:asc,createTime:desc       每项单独指定方向（冒号）
 *   orderBy=+status,-createTime              每项单独指定方向（+/- 前缀）
 * </pre>
 *
 * <p>这里只做语法解析，字段合法性由 {@link top.wyhao.mybatis.query.ColumnResolver} 把关。</p>
 */
public final class SortParser {

    private SortParser() {
    }

    /** 排序字段数量上限，防止前端拼出 order by 几十个字段拖垮数据库 */
    public static final int MAX_SORT_FIELDS = 5;

    /**
     * @param orderBy    原始排序串，可为空
     * @param defaultAsc 未单独指定方向的项使用的方向
     */
    public static List<Sort> parse(String orderBy, boolean defaultAsc) {
        if (orderBy == null || orderBy.isBlank()) {
            return List.of();
        }

        List<Sort> result = new ArrayList<>();
        Set<String> seen = new LinkedHashSet<>();

        for (String raw : orderBy.split(",")) {
            String item = raw.trim();
            if (item.isEmpty()) {
                continue;
            }

            boolean asc = defaultAsc;
            String property;

            char first = item.charAt(0);
            if (first == '-' || first == '+') {
                asc = first == '+';
                property = item.substring(1).trim();
            } else {
                // 按空格或冒号切成 "字段 方向"
                String[] parts = item.split("[\\s:]+", 2);
                property = parts[0].trim();
                if (parts.length == 2) {
                    asc = parseDirection(parts[1].trim(), item);
                }
            }

            if (property.isEmpty()) {
                throw new IllegalArgumentException("非法排序表达式: " + item);
            }
            // 同一字段重复出现时以第一次为准
            if (seen.add(property)) {
                result.add(new Sort(property, asc));
            }
        }

        if (result.size() > MAX_SORT_FIELDS) {
            throw new IllegalArgumentException("排序字段最多 " + MAX_SORT_FIELDS + " 个，当前 " + result.size());
        }
        return result;
    }

    private static boolean parseDirection(String dir, String item) {
        if ("asc".equalsIgnoreCase(dir)) {
            return true;
        }
        if ("desc".equalsIgnoreCase(dir)) {
            return false;
        }
        throw new IllegalArgumentException("非法排序方向: " + item);
    }
}
