package ${package}.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import model.top.wyhao.cmn.db.BaseDO;

import java.io.Serial;

/**
 * ${table.comment!}
 *

 * @since ${date}
 */
@Data
@TableName("${table.name}")
public class ${entity} extends BaseDO {

    @Serial
    private static final long serialVersionUID = 1L;

<#if columns??>
    <#list columns as column>
    <#if !column.pk>
    /**
     * ${column.comment!}
     */
    private ${column.javaType} ${column.javaField};
    </#if>
    </#list>
</#if>
}