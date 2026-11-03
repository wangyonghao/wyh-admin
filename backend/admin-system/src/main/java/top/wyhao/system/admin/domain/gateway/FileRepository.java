package top.wyhao.system.admin.domain.gateway;

import top.wyhao.mybatis.query.PageResult;
import top.wyhao.system.admin.domain.enums.FileCategory;
import top.wyhao.system.admin.domain.model.File;

/**
 * 文件仓储
 */
public interface FileRepository {

    void insert(File file);

    File findById(Long id);

    void updateById(File file);

    /**
     * 分页查询可访问文件
     */
    PageResult<File> page(String fileName, FileCategory category, boolean asc, long page, long size);
}
