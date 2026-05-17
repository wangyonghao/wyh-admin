
package top.wyhao.admin.exception;

import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.servlet.NoHandlerFoundException;
import top.wyhao.admin.system.otp.exception.OtpException;
import top.wyhao.starter.core.constant.StringConstants;
import top.wyhao.starter.core.exception.BusinessException;
import top.wyhao.starter.core.model.R;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 兜底错误处理
 */
@Slf4j
@Order(100)
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * OTP 异常处理
     */
    @ExceptionHandler(OtpException.class)
    public R<Map<String, Object>> handleOtpException(OtpException e, HttpServletRequest request) {
        log.debug("OTP 异常：request={} {} code={}, message={}", request.getMethod(), request.getRequestURI(), e.getCode(), e.getMessage());
        
        Map<String, Object> data = new HashMap<>();
        if (e.getRetryAfter() != null) {
            data.put("retry_after", e.getRetryAfter());
        }
        
        // 根据错误码返回不同的 HTTP 状态码
        HttpStatus status = switch (e.getCode()) {
            case "OTP_NOT_FOUND" -> HttpStatus.NOT_FOUND;
            case "OTP_ALREADY_USED" -> HttpStatus.GONE;
            case "RATE_LIMIT_EXCEEDED" -> HttpStatus.TOO_MANY_REQUESTS;
            case "OTP_LOCKED" -> HttpStatus.LOCKED;
            default -> HttpStatus.BAD_REQUEST;
        };
        R<Map<String, Object>> r = new R<>();
        r.setData(data);
        r.setCode(e.getCode());
        r.setMsg(e.getMessage());
        return r;
    }

    /**
     * 业务校验异常处理(如密码错误、库存不足)
     * 属于业务逻辑分支流程，应提供用户友好、清晰的提示，由用户自行处理
     */
    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    public R<Void> handleBusinessException(BusinessException e, HttpServletRequest request) {
        log.debug("业务阻断：request={} {} code={}, message={}", request.getMethod(), request.getRequestURI(), e.getCode(), e.getMessage());
        log.debug("root cause",ExceptionUtil.getRootCause(e));

        return R.fail(e.getCode(), e.getMessage());
    }


    /**
     * 请求参数缺失异常
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public R<Void> handleMissingServletRequestParameterException(MissingServletRequestParameterException e, HttpServletRequest request) {
        log.debug(CharSequenceUtil.format("[{}] {}", request.getMethod(), request.getRequestURI()), e);
        return R.fail(String.valueOf(HttpStatus.BAD_REQUEST.value()), "参数 '%s' 缺失".formatted(e.getParameterName()));
    }

    /**
     * 参数校验不通过异常
     * <p>
     * {@code @NotBlank}、{@code @NotNull} 等参数验证不通过
     * </p>
     */
    @ExceptionHandler({BindException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public R<Void> handleBindException(BindException e, HttpServletRequest request) {
        log.debug(CharSequenceUtil.format("[{}] {}", request.getMethod(), request.getRequestURI()), e);
        String errorMsg = e.getFieldErrors()
            .stream()
            .findFirst()
            .map(DefaultMessageSourceResolvable::getDefaultMessage)
            .orElse(StringConstants.EMPTY);
        return R.fail(String.valueOf(HttpStatus.BAD_REQUEST.value()), errorMsg);
    }

    /**
     * 方法参数类型不匹配异常
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public R<Void> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e, HttpServletRequest request) {
        log.error(CharSequenceUtil.format("[{}] {}", request.getMethod(), request.getRequestURI()), e);
        return R.fail(String.valueOf(HttpStatus.BAD_REQUEST.value()), "参数 '%s' 类型不匹配".formatted(e.getName()));
    }

    /**
     * HTTP 消息不可读异常
     * <p>
     * 1.@RequestBody 缺失请求体<br />
     * 2.@RequestBody 实体内参数类型不匹配<br />
     * 3.请求体解析格式异常<br />
     * ...
     * </p>
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public R<Void> handleHttpMessageNotReadableException(HttpMessageNotReadableException e, HttpServletRequest request) {
        log.warn(CharSequenceUtil.format("[{}] {}", request.getMethod(), request.getRequestURI()), e);
        // @RequestBody 实体内参数类型不匹配
        if (e.getCause() instanceof InvalidFormatException invalidFormatException) {
            return R.fail(String.valueOf(HttpStatus.BAD_REQUEST.value()), "参数 '%s' 类型不匹配"
                .formatted(invalidFormatException.getValue()));
        }
        return R.fail(String.valueOf(HttpStatus.BAD_REQUEST.value()), "参数缺失或格式不正确");
    }

    /**
     * 文件上传异常-超过上传大小限制
     */
    @ExceptionHandler(MultipartException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public R<Void> handleMultipartException(MultipartException e, HttpServletRequest request) {
        log.error("[{}] {}", request.getMethod(), request.getRequestURI(), e);
        String msg = e.getMessage();
        R<Void> defaultFail = R.fail(String.valueOf(HttpStatus.BAD_REQUEST.value()), msg);
        if (CharSequenceUtil.isBlank(msg)) {
            return defaultFail;
        }
        String sizeLimit;
        Throwable cause = e.getCause();
        if (cause != null) {
            msg = msg.concat(cause.getMessage().toLowerCase());
        }
        if (msg.contains("larger than")) {
            sizeLimit = CharSequenceUtil.subAfter(msg, "larger than ", true);
        } else if (msg.contains("size") && msg.contains("exceed")) {
            sizeLimit = CharSequenceUtil.subBetween(msg, "the maximum size ", " for");
        } else {
            return defaultFail;
        }
        return R.fail(String.valueOf(HttpStatus.BAD_REQUEST.value()), "请上传小于 %s 的文件".formatted(FileUtil
            .readableFileSize(Long.parseLong(sizeLimit))));
    }

    /**
     * 请求 URL 不存在异常
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public R<Void> handleNoHandlerFoundException(NoHandlerFoundException e, HttpServletRequest request) {
        log.error(CharSequenceUtil.format("[{}] {}", request.getMethod(), request.getRequestURI()), e);
        return R.fail(String.valueOf(HttpStatus.NOT_FOUND.value()), "请求 URL '%s' 不存在".formatted(request
            .getRequestURI()));
    }

    /**
     * 不支持的 HTTP 请求方法异常
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    public R<Void> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException e,
                                                          HttpServletRequest request) {
        log.error(CharSequenceUtil.format("[{}] {}", request.getMethod(), request.getRequestURI()), e);
        return R.fail(String.valueOf(HttpStatus.METHOD_NOT_ALLOWED.value()), "请求方式 '%s' 不支持".formatted(e.getMethod()));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public R<Void> handleConstraintViolationException(ConstraintViolationException e, HttpServletRequest request) {
        String msg = e.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining(","));
        log.warn(CharSequenceUtil.format("参数校验失败：request={} {} type={}, message={}", request.getMethod(), request.getRequestURI(), e.getClass().getName(), e.getMessage()),e);
        return R.fail(HttpStatus.BAD_REQUEST.name(), "参数校验失败：" + msg);
    }

    /**
     * 系统故障（兜底）
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public R<Void> handleSystemException(Exception e, HttpServletRequest request) {
        log.error(CharSequenceUtil.format("系统故障：request={} {} type={}, message={}", request.getMethod(), request.getRequestURI(), e.getClass().getName(), e.getMessage()),e);
        return R.fail("SYSTEM_ERROR", "系统繁忙");
    }
}