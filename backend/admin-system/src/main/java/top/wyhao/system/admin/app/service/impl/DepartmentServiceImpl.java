
package top.wyhao.system.admin.app.service.impl;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.wyhao.cmn.core.constant.StringConstants;
import top.wyhao.cmn.core.enums.StatusEnum;
import top.wyhao.cmn.core.util.TreeUtils;
import top.wyhao.system.admin.domain.gateway.UserRepository;
import top.wyhao.system.admin.app.dto.DepartmentQuery;
import top.wyhao.system.admin.app.dto.DepartmentRequest;
import top.wyhao.system.admin.app.dto.DepartmentResult;
import top.wyhao.system.admin.app.assembler.DepartmentAssembler;
import top.wyhao.system.admin.app.service.DepartmentService;
import top.wyhao.system.admin.domain.exception.DepartmentException;
import top.wyhao.system.admin.domain.gateway.DepartmentRepository;
import top.wyhao.system.admin.domain.model.Department;
import top.wyhao.system.admin.domain.gateway.RoleDepartmentRepository;
import top.wyhao.starter.excel.util.ExcelUtils;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * 部门业务实现
 *
 * @since 2023/1/22 17:55
 */
@Service
@RequiredArgsConstructor
public class DepartmentServiceImpl implements DepartmentService {

    private final RoleDepartmentRepository roleDepartmentRepository;
    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final DepartmentAssembler departmentAssembler;

    @Override
    public List<DepartmentResult> list(DepartmentQuery query) {
        List<Department> entityList = departmentRepository.listByQuery(query);
        return departmentAssembler.toResultList(entityList);
    }

    @Override
    public List<DepartmentResult> tree(DepartmentQuery query) {
        List<DepartmentResult> list = this.list(query);
        return TreeUtils.flatToTree(list,
                DepartmentResult::getId,
                DepartmentResult::getParentId,
                DepartmentResult::getChildren,
                (item, children) -> new DepartmentResult(
                        item.getId(),
                        item.getName(),
                        item.getCode(),
                        item.getType(),
                        item.getParentId(),
                        item.getSort(),
                        item.getIsBuiltin(),
                        item.getDescription(),
                        item.getStatus(),
                        item.getDisabled(),
                        children
                ));
    }

    @Override
    public DepartmentResult get(Long id) {
        Department entity = departmentRepository.findById(id);
        return departmentAssembler.toResult(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(DepartmentRequest req) {
        // 验证部门名称是否已存在
        this.checkNameExist(req.getName(), req.getParentId(), null);

        Department entity = new Department();
        entity.setCode(req.getCode());
        entity.setName(req.getName());
        entity.setType(req.getType());
        entity.setParentId(req.getParentId());
        entity.setAncestors(this.calcDepartmentPath(entity.getParentId()));
        entity.setDescription(req.getDescription());
        entity.setSort(req.getSort());
        entity.setStatus(req.getStatus());
        departmentRepository.insert(entity);
        return entity.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(DepartmentRequest req, Long id) {
        this.checkCanUpdate(req, id);

        Department oldEntity = this.require(id);
        String newAncestors = this.calcDepartmentPath(req.getParentId());

        // type、isBuiltin 不可变更
        Department updateEntity = new Department();
        updateEntity.setId(id);
        updateEntity.setCode(req.getCode());
        updateEntity.setName(req.getName());
        updateEntity.setParentId(req.getParentId());
        updateEntity.setAncestors(newAncestors);
        updateEntity.setDescription(req.getDescription());
        updateEntity.setSort(req.getSort());
        updateEntity.setStatus(req.getStatus());
        departmentRepository.updateById(updateEntity);

        // 变更上级部门时，更新所有下级的 ancestors
        if (ObjectUtil.notEqual(req.getParentId(), oldEntity.getParentId())) {
            departmentRepository.updateChildrenAncestors(newAncestors, oldEntity.getAncestors(), id);
        }
    }


    private Department require(Long id) {
        Department department = departmentRepository.findById(id);
        if (department == null) {
            throw DepartmentException.notFound(id);
        }
        return department;
    }

    public void checkCanUpdate(DepartmentRequest req, Long id) {
        // 检查名称是否重复
        if (Objects.nonNull(req.getParentId())) {
            this.checkNameExist(req.getName(), req.getParentId(), id);
        }

        Department oldDepartment = this.require(id);

        if (Boolean.TRUE.equals(oldDepartment.getIsBuiltin())) {
            if (ObjectUtil.equal(StatusEnum.DISABLE.getValue(), req.getStatus())) {
                throw DepartmentException.builtinDisableNotAllowed(oldDepartment.getName());
            }
            if (ObjectUtil.notEqual(req.getParentId(), oldDepartment.getParentId())) {
                throw DepartmentException.builtinParentUpdateNotAllowed(oldDepartment.getName());
            }
        }
        if (ObjectUtil.notEqual(req.getStatus(), oldDepartment.getStatus())) {
            List<Department> children = this.listChildren(id);
            long enabledChildrenCount = children.stream().filter(d -> StatusEnum.ENABLE.getValue().equals(d.getStatus())).count();
            if (StatusEnum.DISABLE.getValue().equals(req.getStatus()) && enabledChildrenCount > 0) {
                throw DepartmentException.hasEnabledChildren(oldDepartment.getName());
            }
            Department oldParentDepartment = this.getByParentId(oldDepartment.getParentId());
            if (StatusEnum.ENABLE.getValue().equals(req.getStatus()) && StatusEnum.DISABLE.getValue()
                    .equals(oldParentDepartment.getStatus())) {
                throw DepartmentException.parentDisabled(oldDepartment.getName());
            }
        }
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(List<Long> ids) {
        List<Department> list = departmentRepository.listByQuery(null).stream()
                .filter(d -> ids.contains(d.getId()))
                .toList();
        Optional<Department> builtinData = list.stream().filter(Department::getIsBuiltin).findFirst();
        if (builtinData.isPresent()) {
            throw DepartmentException.builtinDeleteNotAllowed(builtinData.get().getName());
        }
        if (departmentRepository.countChildren(ids) > 0) {
            throw DepartmentException.hasChildren();
        }
        if (userRepository.countByDepartmentIds(ids) > 0) {
            throw DepartmentException.hasUsers();
        }
        // 删除角色和部门关联
        roleDepartmentRepository.deleteByDepartmentIds(ids);
        departmentRepository.deleteByIds(ids);
    }

    @Override
    public void export(DepartmentQuery query, HttpServletResponse response) {
        List<DepartmentResult> list = this.list(query);
        ExcelUtils.export(list, "导出数据", DepartmentResult.class, response);
    }

    @Override
    public List<Department> listChildren(Long id) {
        return departmentRepository.listChildren(id);
    }

    @Override
    public Department getById(Long departmentId) {
        return departmentRepository.findById(departmentId);
    }

    @Override
    public Department resolveByPath(String departmentPath) {
        if (CharSequenceUtil.isBlank(departmentPath)) {
            throw DepartmentException.pathBlank();
        }
        return departmentPath.contains(StringConstants.SLASH)
            ? findMultiLevelDepartment(departmentPath)
            : findSingleLevelDepartment(departmentPath.trim());
    }

    @Override
    public int countValidDepartmentPaths(Collection<String> departmentPaths) {
        int count = 0;
        for (String path : departmentPaths) {
            try {
                resolveByPath(path);
                count++;
            } catch (Exception ignored) {
                // 无效路径不计入
            }
        }
        return count;
    }

    @Override
    public Map<String, Long> resolveDepartmentIdsByPaths(Collection<String> departmentPaths) {
        Map<String, Long> departmentMap = new HashMap<>();
        for (String departmentName : departmentPaths) {
            Department department = resolveByPath(departmentName);
            departmentMap.put(departmentName, department.getId());
        }
        return departmentMap;
    }

    private Department findMultiLevelDepartment(String departmentPath) {
        String[] pathParts = departmentPath.split(StringConstants.SLASH);
        if (pathParts.length == 0) {
            throw DepartmentException.pathFormatInvalid(departmentPath);
        }
        Department currentDepartment = null;
        Long parentId = 0L;
        for (String part : pathParts) {
            String trimmedPart = part.trim();
            if (CharSequenceUtil.isBlank(trimmedPart)) {
                throw DepartmentException.pathContainsBlank(departmentPath);
            }
            currentDepartment = departmentRepository.findByNameAndParentId(trimmedPart, parentId);
            if (currentDepartment == null) {
                throw DepartmentException.notFoundInPath(trimmedPart, departmentPath);
            }
            parentId = currentDepartment.getId();
        }
        return currentDepartment;
    }

    private Department findSingleLevelDepartment(String departmentName) {
        List<Department> departmentList = departmentRepository.listByName(departmentName);
        if (ObjectUtil.isEmpty(departmentList)) {
            throw DepartmentException.notFoundByName(departmentName);
        }
        if (departmentList.size() > 1) {
            throw DepartmentException.nameDuplicate(departmentName);
        }
        return departmentList.get(0);
    }

    /**
     * 检查名称是否重复
     *
     * @param name     名称
     * @param parentId 上级 ID
     * @param selfId   ID
     */
    private void checkNameExist(String name, Long parentId, Long selfId) {
        boolean exists = departmentRepository.existsName(name, parentId, selfId);

        if (exists) {
            throw DepartmentException.nameExist(name);
        }
    }

    /**
     * 获取祖级列表
     *
     * @param parentId 上级部门
     * @return 祖级列表
     */
    private String calcDepartmentPath(Long parentId) {
        Department parentDepartment = this.getByParentId(parentId);
        return "%s,%s".formatted(parentDepartment.getAncestors(), parentId);
    }

    /**
     * 根据上级部门 ID 查询
     *
     * @param parentId 上级部门 ID
     * @return 上级部门信息
     */
    private Department getByParentId(Long parentId) {
        Department parentDepartment = departmentRepository.findById(parentId);
        if (parentDepartment == null) {
            throw DepartmentException.parentNotFound();
        }
        return parentDepartment;
    }
}
