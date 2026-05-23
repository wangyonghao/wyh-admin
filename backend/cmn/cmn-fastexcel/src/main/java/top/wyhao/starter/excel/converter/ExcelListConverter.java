
package top.wyhao.starter.excel.converter;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.idev.excel.converters.Converter;
import cn.idev.excel.enums.CellDataTypeEnum;
import cn.idev.excel.metadata.GlobalConfiguration;
import cn.idev.excel.metadata.data.ReadCellData;
import cn.idev.excel.metadata.data.WriteCellData;
import cn.idev.excel.metadata.property.ExcelContentProperty;
import top.wyhao.starter.core.constant.StringConstants;

import java.util.List;

/**
 * Excel List 集合转换器
 *
 * <p>
 * 仅适合 List<基本类型> <=> xxx,xxx 转换
 * </p>
 *

 * @since 2.0.2
 */
public class ExcelListConverter implements Converter<List> {

    @Override
    public Class supportJavaTypeKey() {
        return List.class;
    }

    @Override
    public CellDataTypeEnum supportExcelTypeKey() {
        return CellDataTypeEnum.STRING;
    }

    @Override
    public List convertToJavaData(ReadCellData<?> cellData,
                                  ExcelContentProperty contentProperty,
                                  GlobalConfiguration globalConfiguration) {
        String stringValue = cellData.getStringValue();
        return CharSequenceUtil.split(stringValue, StringConstants.COMMA);
    }

    @Override
    public WriteCellData<Object> convertToExcelData(List value,
                                                    ExcelContentProperty contentProperty,
                                                    GlobalConfiguration globalConfiguration) {
        WriteCellData<Object> writeCellData = new WriteCellData<>(CollUtil.join(value, StringConstants.COMMA));
        writeCellData.setType(CellDataTypeEnum.STRING);
        return writeCellData;
    }
}
