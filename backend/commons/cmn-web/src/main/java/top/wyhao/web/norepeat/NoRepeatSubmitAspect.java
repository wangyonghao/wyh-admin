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

package top.wyhao.web.norepeat;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.json.JSONUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.validation.BindingResult;
import org.springframework.web.multipart.MultipartFile;
import top.wyhao.redisson.util.RedisUtils;
import top.wyhao.satoken.util.LoginUtil;
import top.wyhao.web.http.ServletUtils;

import java.time.Duration;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;

/**
 * 防重复提交切面
 */
@Aspect
public class NoRepeatSubmitAspect {

    public static final String CACHEK_KEY_PREFIX = "prevent-duplicate-submit:";

    /**
     * 防重复提交处理
     *
     * @param joinPoint             切点
     * @param noRepeatSubmit 防重复提交注解
     * @return 目标方法的执行结果
     * @throws Throwable /
     */
    @Around("@annotation(noRepeatSubmit)")
    public Object around(ProceedingJoinPoint joinPoint, NoRepeatSubmit noRepeatSubmit) throws Throwable {
        long interval = noRepeatSubmit.unit().toMillis(noRepeatSubmit.interval());
        String requestKey = this.getRequestKey(joinPoint, Objects.requireNonNull(ServletUtils.getRequest()));
        String cacheKey = CACHEK_KEY_PREFIX + requestKey;

        // 如果键已存在，则抛出异常
        if (!RedisUtils.setIfAbsent(cacheKey, "", Duration.ofMillis(interval))) {
            throw new DuplicateRequestException(noRepeatSubmit.message());
        }
        // 执行目标方法
        try {
            return joinPoint.proceed();
        } catch (Throwable e) {
            // 删除键
            RedisUtils.delete(cacheKey);
            throw e;
        }
    }

    /**
     * 根据请求内容获取唯一Key
     *
     * @param joinPoint 切点
     * @return 请求唯一Key
     */
    private String getRequestKey(ProceedingJoinPoint joinPoint, HttpServletRequest request) {
        String token = StrUtil.trimToEmpty(LoginUtil.getTokenValue());
        String className = ClassUtil.getClassName(joinPoint.getTarget(), false); // e.g. com.example.demo.controller.UserController
        String methodName = joinPoint.getSignature().getName();
        String argsString = this.argsToString(joinPoint.getArgs());
        return SecureUtil.md5(token + ":" + className + ":" + methodName + ":" + argsString);
    }

    /**
     * 将方法所有参数拼装为字符串
     */
    private String argsToString(Object[] args) {
        if (ArrayUtil.isEmpty(args)) {
            return StrUtil.EMPTY;
        }
        StringJoiner params = new StringJoiner(",");
        for (Object o : args) {
            if (ObjectUtil.isNotNull(o) && !isSpecialParamType(o)) {
                params.add(JSONUtil.toJsonStr(o));
            }
        }
        return params.toString();
    }

    /**
     * 判断是否特殊参数
     *
     * @param o 对象信息。
     * @return 如果是需要过滤的对象，则返回true；否则返回false。
     */
    @SuppressWarnings("rawtypes")
    private boolean isSpecialParamType(final Object o) {
        Class<?> clazz = o.getClass();
        if (clazz.isArray()) {
            return MultipartFile.class.isAssignableFrom(clazz.getComponentType());
        } else if (Collection.class.isAssignableFrom(clazz)) {
            Collection collection = (Collection) o;
            for (Object value : collection) {
                return value instanceof MultipartFile;
            }
        } else if (Map.class.isAssignableFrom(clazz)) {
            Map map = (Map) o;
            for (Object value : map.values()) {
                return value instanceof MultipartFile;
            }
        }
        return o instanceof MultipartFile
                || o instanceof HttpServletRequest
                || o instanceof HttpServletResponse
                || o instanceof BindingResult;
    }
}