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

package top.wyhao.web.log;

import cn.hutool.extra.spring.SpringUtil;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.util.StopWatch;
import top.wyhao.satoken.util.LoginUtil;
import top.wyhao.cmn.identity.LoginUser;
import top.wyhao.web.http.ServletUtils;

import java.time.LocalDateTime;

/**
 * 日志切面
 */
@Slf4j
@Aspect
public class OperationLogAspect {
    /**
     * 记录开始时间的 ThreadLocal
     */
    private static final ThreadLocal<StopWatch> START_TIME = new ThreadLocal<>();


    /**
     * 方法执行前记录开始时间
     */
    @Before("@annotation(controllerLog)")
    public void doBefore(JoinPoint joinPoint, OperationLogAnn controllerLog) {
        StopWatch stopWatch = new StopWatch();
        START_TIME.set(stopWatch);
        stopWatch.start();
    }

    /**
     * 处理完请求后执行
     */
    @AfterReturning(pointcut = "@annotation(controllerLog)", returning = "jsonResult")
    public void doAfterReturning(JoinPoint joinPoint, OperationLogAnn controllerLog, Object jsonResult) {
        handleLog(joinPoint, controllerLog, null, jsonResult);
    }

    /**
     * 拦截异常操作
     */
    @AfterThrowing(pointcut = "@annotation(controllerLog)", throwing = "e")
    public void doAfterThrowing(JoinPoint joinPoint, OperationLogAnn controllerLog, Exception e) {
        handleLog(joinPoint, controllerLog, e, null);
    }


    /**
     * 处理日志
     */
    protected void handleLog(final JoinPoint joinPoint, OperationLogAnn controllerLog, final Exception e, Object jsonResult) {
        try {
            Long objectId = getObjectId(joinPoint, controllerLog.objectIdParam());
            String ip = ServletUtils.getRequestIp();

            OperationLog operationLog = new OperationLog();
            operationLog.setObjectId(objectId);
            operationLog.setObjectType(controllerLog.objectType());
            operationLog.setOperation(controllerLog.operationType());
            if (LoginUtil.getLoginUser() != null) {
                LoginUser user = LoginUtil.getLoginUser();
                operationLog.setOperatorId(user.getUserId());
                operationLog.setOperatorName(user.getUsername());
            }
            operationLog.setOperatorIp(ip);
            operationLog.setOperateTime(LocalDateTime.now());
            operationLog.setStatus("true");

            // 计算耗时
            StopWatch stopWatch = START_TIME.get();
            stopWatch.stop();
            operationLog.setElapsedMills(stopWatch.getTotalTimeMillis());

            // 异步保存日志
            SpringUtil.getApplicationContext().publishEvent(operationLog);
        } catch (Exception ex) {
            log.error("记录操作日志异常：", ex);
        } finally {
            START_TIME.remove();
        }
    }

    private Long getObjectId(JoinPoint joinPoint, String objectIdParam){
        return 0L;
    }
}
