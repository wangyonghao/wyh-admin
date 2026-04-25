/*
 * Copyright (c) 2022-present wangyonghao Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package top.wyhao.admin.system.mapper;

import cn.hutool.core.text.CharSequenceUtil;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import top.wyhao.admin.system.model.entity.SettingsDO;
import top.wyhao.starter.data.mapper.BaseMapper;

import java.util.List;

/**
 * 参数 Mapper
 *
 * @author wyh
 * @since 2025/11/30
 */
@Mapper
public interface SettingsMapper extends BaseMapper<SettingsDO> {

    /**
     * 根据类别查询
     *
     * @param category 类别
     * @return 列表
     */
    @Select("SELECT code, value, default_value FROM sys_settings WHERE category = #{category}")
    List<SettingsDO> selectByCategory(@Param("category") String category);

    /**
     * 获取原始值
     *
     * @param key 键
     * @return 值
     */
    default String getRawValue(String key) {
        SettingsDO config = this.lambdaQuery()
                .eq(SettingsDO::getCode, key)
                .select(SettingsDO::getValue, SettingsDO::getDefaultValue)
                .one();
        if (config == null) {
            return null;
        }
        return CharSequenceUtil.nullToDefault(config.getValue(), config.getDefaultValue());
    }

    /**
     * 根据编码查询参数值（自动转换为 int 类型）
     *
     * @param key 键
     * @param defaultValue 默认值
     * @return 整数值
     */
    default int getInt(String key, int defaultValue) {
        String value = getRawValue(key);
        return CharSequenceUtil.isBlank(value) ? defaultValue : Integer.parseInt(value);
    }

    /**
     * 获取布尔值
     *
     * @param key 键
     * @return 布尔值
     */
    default Boolean getBoolean(String key) {
        String value = getRawValue(key);
        return CharSequenceUtil.isBlank(value) ? null : Boolean.parseBoolean(value);
    }
}
