package top.wyhao.system.admin.app.assembler;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import top.wyhao.system.admin.domain.model.Menu;
import top.wyhao.system.admin.app.dto.MenuTreeVO;
import top.wyhao.system.admin.app.dto.MenuVO;
import top.wyhao.web.convert.MapStructConfig;

import java.util.List;
import top.wyhao.system.admin.app.dto.MenuRequest;

/**
 * 菜单对象转换
 */
@Mapper(config = MapStructConfig.class)
public interface MenuAssembler {

    MenuVO toVO(Menu menu);

    List<MenuVO> toVOList(List<Menu> menus);

    MenuTreeVO toTreeVO(Menu menu);

    List<MenuTreeVO> toTreeVOList(List<Menu> menus);

    Menu toEntity(MenuRequest request);

    @Mapping(target = "type", ignore = true)
    void update(MenuRequest request, @MappingTarget Menu entity);
}
