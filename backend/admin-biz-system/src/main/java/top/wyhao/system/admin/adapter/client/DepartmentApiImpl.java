package top.wyhao.system.admin.adapter.client;

import cn.hutool.core.util.ObjectUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import top.wyhao.cmn.core.enums.StatusEnum;
import top.wyhao.cmn.core.util.CollUtils;
import top.wyhao.system.admin.app.service.DepartmentService;
import top.wyhao.system.admin.domain.model.Department;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 部门业务 API 适配：只委托 app。
 */
@Service
@RequiredArgsConstructor
public class DepartmentApiImpl implements DepartmentApi {

    private final DepartmentService departmentService;

    @Override
    public List<Long> listSelfAndDescendantIds(Long departmentId) {
        List<Long> departmentIdList = CollUtils.mapToList(departmentService.listChildren(departmentId), Department::getId);
        departmentIdList.add(departmentId);
        return departmentIdList;
    }

    @Override
    public int countValidDepartmentPaths(Collection<String> departmentPaths) {
        return departmentService.countValidDepartmentPaths(departmentPaths);
    }

    @Override
    public Map<String, Long> resolveDepartmentIdsByPaths(Collection<String> departmentPaths) {
        return departmentService.resolveDepartmentIdsByPaths(departmentPaths);
    }

    @Override
    public boolean isDisabled(Long departmentId) {
        Department department = departmentService.getById(departmentId);
        return department != null && ObjectUtil.equal(StatusEnum.DISABLE.getValue(), department.getStatus());
    }
}
