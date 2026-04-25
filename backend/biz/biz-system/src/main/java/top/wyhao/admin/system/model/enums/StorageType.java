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

package top.wyhao.admin.system.model.enums;

import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.dromara.x.file.storage.core.FileStorageProperties;
import org.dromara.x.file.storage.spring.SpringFileStorageProperties;
import top.wyhao.starter.core.constant.RegexConstants;
import top.wyhao.admin.system.model.bo.StorageReq;
import top.wyhao.admin.system.model.validation.ValidationGroup;
import top.wyhao.starter.core.constant.StringConstants;
import top.wyhao.starter.core.util.URLUtils;
import top.wyhao.starter.core.util.validation.ValidationUtils;

/**
 * 存储类型枚举
 *
 * @author Charles7c
 * @since 2023/12/27 21:45
 */
@Getter
@RequiredArgsConstructor
public enum StorageType {

    /**
     * 本地存储
     */
    LOCAL("local", "本地存储", SpringFileStorageProperties.SpringLocalPlusConfig.class) {
        @Override
        public void validate(StorageReq req) {
            ValidationUtils.validate(req, ValidationGroup.Storage.Local.class);
            ValidationUtils.throwIf(StrUtil.isNotBlank(req.getDomain()) && !URLUtils.isHttpUrl(req
                .getDomain()), "访问路径格式不正确");
        }

        @Override
        public void pretreatment(StorageReq req) {
            super.pretreatment(req);
            // 本地存储路径需要以 “/” 结尾
            req.setBucketName(StrUtil.appendIfMissing(req.getBucketName(), StringConstants.SLASH));
        }
    },
    MINIO("minio", "MinIO存储", SpringFileStorageProperties.SpringMinioConfig.class){
        @Override
        public void validate(StorageReq req) {
            ValidationUtils.validate(req, ValidationGroup.Storage.class);
            ValidationUtils.throwIf(StrUtil.isNotBlank(req.getDomain()) && !URLUtils.isHttpUrl(req
                .getDomain()), "访问路径格式不正确");
        }
    },
    /**
     * 对象存储
     */
    OSS("2", "对象存储", FileStorageProperties.AmazonS3Config.class) {
        @Override
        public void validate(StorageReq req) {
            ValidationUtils.validate(req, ValidationGroup.Storage.OSS.class);
            ValidationUtils.throwIf(StrUtil.isNotBlank(req.getDomain()) && !ReUtil
                .isMatch(RegexConstants.URL_HTTP_NOT_IP, req.getDomain()), "域名格式不正确");
        }
    };

    private final String value;
    private final String description;
    private final Class<? extends FileStorageProperties.BaseConfig> configClass;

    /**
     * 校验
     *
     * @param req 请求参数
     */
    public abstract void validate(StorageReq req);

    /**
     * 处理参数
     *
     * @param req 请求参数
     */
    public void pretreatment(StorageReq req) {
        // 域名需要以 “/” 结尾（x-file-storage 在拼接路径时都是直接 + 拼接，所以规范要求每一级都要以 “/” 结尾，且后面路径不能以 “/” 开头）
        if (StrUtil.isNotBlank(req.getDomain())) {
            req.setDomain(StrUtil.appendIfMissing(req.getDomain(), StringConstants.SLASH));
        }
    }

    public static Class<? extends FileStorageProperties.BaseConfig> getConfigClass(String platform) {
        for (StorageType item : values()) {
            if (platform.startsWith(item.getValue())) {
                return item.getConfigClass();
            }
        }
        return null;
    }
}
