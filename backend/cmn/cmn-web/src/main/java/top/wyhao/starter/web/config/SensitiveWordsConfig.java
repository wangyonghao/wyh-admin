/*
 * Copyright (c) 2022-present Charles7c Authors. All Rights Reserved.
 * <p>
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE 3.0;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.gnu.org/licenses/lgpl.html
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package top.wyhao.starter.web.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import top.wyhao.starter.core.constant.PropertiesConstants;

import java.util.List;

/**
 * 敏感词自动配置
 *


 * @since 2.9.0
 */
@AutoConfiguration
@EnableConfigurationProperties(SensitiveWordsConfig.SensitiveWordsProperties.class)
public class SensitiveWordsConfig {

    private static final Logger log = LoggerFactory.getLogger(SensitiveWordsConfig.class);

    /**
     * 敏感词配置属性
     */
    @ConfigurationProperties("security.sensitive-words")
    static class SensitiveWordsProperties {

        /**
         * 敏感词列表
         */
        private List<String> values;

        public List<String> getValues() {
            return values;
        }

        public void setValues(List<String> values) {
            this.values = values;
        }
    }

}
