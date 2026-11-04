package top.wyhao.system.admin.infrastructure.persistence;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import top.wyhao.mybatis.query.PageResult;
import top.wyhao.system.admin.domain.enums.FileCategory;
import top.wyhao.system.admin.domain.enums.FileStatus;
import top.wyhao.system.admin.domain.gateway.FileRepository;
import top.wyhao.system.admin.domain.model.File;
import top.wyhao.system.admin.infrastructure.persistence.AdminDoMapper;
import top.wyhao.system.admin.infrastructure.persistence.dataobject.SysFileDO;
import top.wyhao.system.admin.infrastructure.persistence.mapper.FileMapper;

/**
 * 文件仓储实现
 */
@Repository
@RequiredArgsConstructor
public class FileRepositoryImpl implements FileRepository {

    private final FileMapper fileMapper;

    @Override
    public void insert(File file) {
        SysFileDO data = AdminDoMapper.toDo(file, SysFileDO.class);
        fileMapper.insert(data);
        AdminDoMapper.copyBack(data, file);
    }

    @Override
    public File findById(Long id) {
        return AdminDoMapper.toDomain(fileMapper.selectById(id), File.class);
    }

    @Override
    public void updateById(File file) {
        SysFileDO data = AdminDoMapper.toDo(file, SysFileDO.class);
        fileMapper.updateById(data);
        AdminDoMapper.copyBack(data, file);
    }

    @Override
    public PageResult<File> page(String fileName, FileCategory category, boolean asc, long page, long size) {
        var query = new LambdaQueryWrapper<SysFileDO>()
                .eq(SysFileDO::getStatus, FileStatus.ACTIVE)
                .like(StrUtil.isNotBlank(fileName), SysFileDO::getFileName, fileName);

        if (category != FileCategory.ALL && !category.getExtensions().isEmpty()) {
            query.and(wrapper -> {
                boolean first = true;
                for (String ext : category.getExtensions()) {
                    if (first) {
                        wrapper.likeLeft(SysFileDO::getFileName, "." + ext);
                        first = false;
                    } else {
                        wrapper.or().likeLeft(SysFileDO::getFileName, "." + ext);
                    }
                }
            });
        }

        if (asc) {
            query.orderByAsc(SysFileDO::getCreateTime);
        } else {
            query.orderByDesc(SysFileDO::getCreateTime);
        }
        return PageResult.of(fileMapper.selectPage(new Page<>(page, size), query))
                .map(item -> AdminDoMapper.toDomain(item, File.class));
    }
}
