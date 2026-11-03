package top.wyhao.system.admin.app.assembler;

import org.mapstruct.Mapper;
import top.wyhao.system.admin.domain.model.Department;
import top.wyhao.web.convert.MapStructConfig;

import java.util.List;
import top.wyhao.system.admin.app.dto.DepartmentResult;

/**
 * 部门对象转换
 */
@Mapper(config = MapStructConfig.class)
public interface DepartmentAssembler {

    DepartmentResult toResult(Department department);

    List<DepartmentResult> toResultList(List<Department> departments);
}
