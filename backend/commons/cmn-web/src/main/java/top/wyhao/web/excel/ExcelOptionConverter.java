package top.wyhao.web.excel;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.extra.spring.SpringUtil;
import cn.idev.excel.converters.Converter;
import cn.idev.excel.metadata.GlobalConfiguration;
import cn.idev.excel.metadata.data.ReadCellData;
import cn.idev.excel.metadata.data.WriteCellData;
import cn.idev.excel.metadata.property.ExcelContentProperty;
import top.wyhao.cmn.core.constant.StringConstants;
import top.wyhao.web.core.model.LabelValueResult;

import java.util.List;
import java.util.Objects;

/**
 * Easy Excel 选项转换器
 */
public class ExcelOptionConverter implements Converter<Object> {

    @Override
    public Object convertToJavaData(ReadCellData<?> cellData,
                                    ExcelContentProperty contentProperty,
                                    GlobalConfiguration globalConfiguration) {
        List<LabelValueResult<String>> options = this.getOptions(contentProperty);
        String value = options.stream()
            .filter(item -> Objects.equals(cellData.getStringValue(), item.getValue()))
            .findFirst()
            .map(LabelValueResult::getLabel)
            .orElse(null);
        return Convert.convert(contentProperty.getField().getType(), value);
    }

    @Override
    public WriteCellData<String> convertToExcelData(Object data,
                                                    ExcelContentProperty contentProperty,
                                                    GlobalConfiguration globalConfiguration) {
        if (data == null) {
            return new WriteCellData<>(StringConstants.EMPTY);
        }
        List<LabelValueResult<String>> options = this.getOptions(contentProperty);
        if (CollUtil.isEmpty(options)) {
            return new WriteCellData<>(StringConstants.EMPTY);
        }
        return new WriteCellData<>(options.stream()
            .filter(item -> Objects.equals(data, item.getValue()))
            .findFirst()
            .map(LabelValueResult::getLabel)
            .orElse(StringConstants.EMPTY));
    }

    private List<LabelValueResult<String>> getOptions(ExcelContentProperty contentProperty) {
        OptionExcelProperty property = contentProperty.getField().getAnnotation(OptionExcelProperty.class);
        if (property == null) {
            throw new IllegalArgumentException("Excel 选项转换器异常：请为字段添加 @OptionExcelProperty 注解");
        }
        OptionApi optionApi = SpringUtil.getBean(OptionApi.class);
        return optionApi.listByType(property.value());
    }
}
