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

package top.wyhao.web.validation;

import cn.hutool.dfa.WordTree;
import jakarta.annotation.PostConstruct;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;

/**
 * 敏感词校验器
 */
public class SensitiveWordsValidator implements ConstraintValidator<SensitiveWords, String> {

    private final WordTree tree = new WordTree();

    @Value("${security.sensitive-words:[]}")
    private String[] words;

    @PostConstruct
    public void postConstruct() {
        tree.addWords(words);
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        List<String> matchedWords = tree.matchAll(value, -1, false, true);
        if (!matchedWords.isEmpty()) {
            // 禁用默认消息
            context.disableDefaultConstraintViolation();
            // 动态设置错误消息
            context.buildConstraintViolationWithTemplate("内容包含敏感词汇: " + String.join(",", matchedWords)).addConstraintViolation();
            return false;
        }
        return true;
    }
}