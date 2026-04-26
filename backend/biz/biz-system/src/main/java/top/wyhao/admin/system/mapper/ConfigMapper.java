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

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import top.wyhao.admin.system.model.entity.ConfigDO;
import top.wyhao.admin.system.model.vo.ConfigResult;
import top.wyhao.starter.data.mapper.BaseMapper;

/**
 * 系统配置 Mapper
 *
 * @author wyhao
 * @since 2024/04/26
 */
@Mapper
public interface ConfigMapper extends BaseMapper<ConfigDO> {

    /**
     * 分页查询
     *
     * @param page         分页对象
     * @param queryWrapper 查询条件
     * @return 分页结果
     */
    IPage<ConfigResult> selectConfigPage(IPage<ConfigResult> page, @Param(Constants.WRAPPER) Wrapper<ConfigDO> queryWrapper);
}
