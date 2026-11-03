package top.wyhao.system.admin.app.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 按当前同级顺序保存菜单上级和排序。
 */
@Data
@Schema(description = "菜单排序")
public class MenuSortRequest {

    @NotEmpty
    @Valid
    private List<Item> items;

    @Data
    @Schema(description = "一个菜单的新位置")
    public static class Item {

        @NotNull
        private Long id;

        @NotNull
        private Long parentId;

        @NotNull
        @Min(0)
        private Integer sort;
    }
}
