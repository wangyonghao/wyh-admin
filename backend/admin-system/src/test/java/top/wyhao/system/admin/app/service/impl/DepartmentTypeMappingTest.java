package top.wyhao.system.admin.app.service.impl;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import top.wyhao.system.admin.app.assembler.DepartmentAssembler;
import top.wyhao.system.admin.app.dto.DepartmentQuery;
import top.wyhao.system.admin.app.dto.DepartmentRequest;
import top.wyhao.system.admin.app.dto.DepartmentResult;
import top.wyhao.system.admin.app.service.DepartmentService;
import top.wyhao.system.admin.domain.gateway.DepartmentRepository;
import top.wyhao.system.admin.domain.gateway.RoleDepartmentRepository;
import top.wyhao.system.admin.domain.gateway.UserRepository;
import top.wyhao.system.admin.domain.model.Department;
import top.wyhao.system.admin.infrastructure.persistence.AdminDoMapper;
import top.wyhao.system.admin.infrastructure.persistence.dataobject.SysDepartmentDO;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 部门类型与 {@code sys_department.type varchar(20)} 对齐为字符串。
 * 用 {@code parseInt} 会截成 1 的非纯数字值，以及 20/21 字符边界。
 */
@ExtendWith(MockitoExtension.class)
class DepartmentTypeMappingTest {

    /** 20 个字符，且以数字开头，Integer / parseInt 都无法原样保留。 */
    private static final String TYPE_20 = "1A-BRANCH-ABCDEFGHIJ";

    private static final String TYPE_21 = TYPE_20 + "K";

    private static final Long DEPARTMENT_ID = 99L;

    private static final Long PARENT_ID = 10L;

    private static Validator validator;

    @Mock
    private RoleDepartmentRepository roleDepartmentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private DepartmentRepository departmentRepository;

    private DepartmentService departmentService;

    @BeforeAll
    static void setUpValidator() {
        assertThat(TYPE_20).hasSize(20);
        assertThat(TYPE_21).hasSize(21);
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @BeforeEach
    void setUp() {
        DepartmentAssembler assembler = Mappers.getMapper(DepartmentAssembler.class);
        departmentService = new DepartmentServiceImpl(
                roleDepartmentRepository,
                userRepository,
                departmentRepository,
                assembler);
    }

    @Test
    void requestAcceptsNonNumericTypeAtVarcharLimit() {
        assertThat(validator.validate(request(TYPE_20))).isEmpty();
        assertThat(validator.validate(request("1A"))).isEmpty();
        assertThat(validator.validate(request("BRANCH"))).isEmpty();
    }

    @Test
    void requestRejectsBlankAndTypeLongerThanColumn() {
        assertThat(typeViolations(null)).extracting(this::path).containsExactly("type");
        assertThat(typeViolations("   ")).extracting(this::path).containsExactly("type");
        assertThat(typeViolations(TYPE_21)).extracting(this::path).containsExactly("type");
    }

    @Test
    void createAndQueryKeepNonNumericType() {
        when(departmentRepository.findById(PARENT_ID)).thenReturn(parent());

        departmentService.create(request(TYPE_20));

        ArgumentCaptor<Department> inserted = ArgumentCaptor.forClass(Department.class);
        verify(departmentRepository).insert(inserted.capture());
        assertThat(inserted.getValue().getType()).isEqualTo(TYPE_20);

        Department stored = inserted.getValue();
        stored.setId(DEPARTMENT_ID);
        when(departmentRepository.findById(DEPARTMENT_ID)).thenReturn(stored);
        when(departmentRepository.listByQuery(any())).thenReturn(List.of(stored));

        DepartmentResult detail = departmentService.get(DEPARTMENT_ID);
        assertThat(detail.getType()).isEqualTo(TYPE_20);

        List<DepartmentResult> tree = departmentService.tree(new DepartmentQuery());
        assertThat(tree).singleElement().extracting(DepartmentResult::getType).isEqualTo(TYPE_20);

        SysDepartmentDO dataObject = AdminDoMapper.toDo(stored, SysDepartmentDO.class);
        assertThat(dataObject.getType()).isEqualTo(TYPE_20);
        assertThat(AdminDoMapper.toDomain(dataObject, Department.class).getType()).isEqualTo(TYPE_20);
    }

    @Test
    void updateLeavesStoredTypeUntouched() {
        Department stored = stored(TYPE_20);
        when(departmentRepository.findById(DEPARTMENT_ID)).thenReturn(stored);
        when(departmentRepository.findById(PARENT_ID)).thenReturn(parent());

        DepartmentRequest req = request("BRANCH");
        departmentService.update(req, DEPARTMENT_ID);

        ArgumentCaptor<Department> updated = ArgumentCaptor.forClass(Department.class);
        verify(departmentRepository).updateById(updated.capture());
        assertThat(updated.getValue().getType()).isNull();
        assertThat(departmentService.get(DEPARTMENT_ID).getType()).isEqualTo(TYPE_20);
    }

    private DepartmentRequest request(String type) {
        DepartmentRequest req = new DepartmentRequest();
        req.setCode("A01");
        req.setName("研发部");
        req.setParentId(PARENT_ID);
        req.setType(type);
        req.setSort(1);
        req.setStatus(1);
        return req;
    }

    private Set<ConstraintViolation<DepartmentRequest>> typeViolations(String type) {
        return validator.validate(request(type)).stream()
                .filter(violation -> "type".equals(violation.getPropertyPath().toString()))
                .collect(Collectors.toSet());
    }

    private String path(ConstraintViolation<DepartmentRequest> violation) {
        return violation.getPropertyPath().toString();
    }

    private Department parent() {
        Department parent = new Department();
        parent.setId(PARENT_ID);
        parent.setAncestors("0");
        parent.setStatus(1);
        return parent;
    }

    private Department stored(String type) {
        Department department = new Department();
        department.setId(DEPARTMENT_ID);
        department.setCode("A01");
        department.setName("研发部");
        department.setType(type);
        department.setParentId(PARENT_ID);
        department.setAncestors("0,10");
        department.setSort(1);
        department.setStatus(1);
        department.setIsBuiltin(false);
        return department;
    }
}
