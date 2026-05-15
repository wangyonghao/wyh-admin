
package top.wyhao.starter.web.http;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.extra.servlet.JakartaServletUtil;
import cn.hutool.http.useragent.UserAgent;
import cn.hutool.http.useragent.UserAgentUtil;
import cn.hutool.json.JSONUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.MediaType;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.util.UriUtils;
import top.wyhao.starter.core.constant.StringConstants;
import top.wyhao.starter.core.util.ExceptionUtils;
import top.wyhao.starter.core.util.IpUtils;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

/**
 * Servlet 工具类
 *
 * @author Charles7c
 * @author echo
 * @since 1.0.0
 */
public class ServletUtils extends JakartaServletUtil {

    private ServletUtils() {
    }

    /**
     * 获取浏览器及其版本信息
     *
     * @param request 请求对象
     * @return 浏览器及其版本信息
     */
    public static String getBrowser(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        return getBrowser(request.getHeader("User-Agent"));
    }

    /**
     * 获取浏览器及其版本信息
     *
     * @param userAgentString User-Agent 字符串
     * @return 浏览器及其版本信息
     */
    public static String getBrowser(String userAgentString) {
        try {
            UserAgent userAgent = UserAgentUtil.parse(userAgentString);
            if (userAgent == null || userAgent.getBrowser() == null) {
                return null;
            }
            String browserName = userAgent.getBrowser().getName();
            String version = userAgent.getVersion();
            return CharSequenceUtil.isBlank(version) ? browserName : browserName + StringConstants.SPACE + version;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 获取操作系统
     *
     * @param request 请求对象
     * @return 操作系统
     */
    public static String getOs(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        return getOs(request.getHeader("User-Agent"));
    }

    /**
     * 获取操作系统
     *
     * @param userAgentString User-Agent 字符串
     * @return 操作系统
     */
    public static String getOs(String userAgentString) {
        try {
            UserAgent userAgent = UserAgentUtil.parse(userAgentString);
            if (userAgent == null || userAgent.getOs() == null) {
                return null;
            }
            return userAgent.getOs().getName();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 获取请求方法
     *
     * @return {@link String }
     * @since 2.11.0
     */
    public static String getRequestMethod() {
        HttpServletRequest request = getRequest();
        return request != null ? request.getMethod() : null;
    }

    /**
     * 获取请求参数
     *
     * @param name 参数名
     * @return {@link String }
     * @since 2.11.0
     */
    public static String getRequestParameter(String name) {
        HttpServletRequest request = getRequest();
        return request != null ? request.getParameter(name) : null;
    }

    /**
     * 获取请求 Ip
     *
     * @return {@link String }
     * @since 2.11.0
     */
    public static String getRequestIp() {
        HttpServletRequest request = getRequest();
        return request != null ? getClientIP(request) : null;
    }

    /**
     * 获取请求头信息
     *
     * @return {@link Map }<{@link String }, {@link String }>
     * @since 2.11.0
     */
    public static Map<String, String> getRequestHeaders() {
        HttpServletRequest request = getRequest();
        return request != null ? getHeaderMap(request) : Collections.emptyMap();
    }

    /**
     * 获取请求 URL（包含 query 参数）
     * <p>{@code http://localhost:8000/system/user?page=1&size=10}</p>
     *
     * @return {@link URI }
     * @since 2.11.0
     */
    public static URI getRequestUrl() {
        HttpServletRequest request = getRequest();
        if (request == null) {
            return null;
        }
        String queryString = request.getQueryString();
        if (CharSequenceUtil.isBlank(queryString)) {
            return URI.create(request.getRequestURL().toString());
        }
        try {
            StringBuilder urlBuilder = appendQueryString(queryString);
            return new URI(urlBuilder.toString());
        } catch (URISyntaxException e) {
            String encoded = UriUtils.encodeQuery(queryString, StandardCharsets.UTF_8);
            StringBuilder urlBuilder = appendQueryString(encoded);
            return URI.create(urlBuilder.toString());
        }
    }

    /**
     * 获取请求路径
     *
     * @return {@link URI }
     * @since 2.11.0
     */
    public static String getRequestPath() {
        HttpServletRequest request = getRequest();
        return request != null ? request.getRequestURI() : null;
    }

    /**
     * 获取请求 body 参数
     *
     * @return {@link String }
     * @since 2.11.0
     */
    public static String getRequestBody() {
        HttpServletRequest request = getRequest();
        if (request instanceof RepeatReadRequestWrapper wrapper && !wrapper.isMultipartContent(request)) {
            String body = JakartaServletUtil.getBody(request);
            return JSONUtil.isTypeJSON(body) ? body : null;
        }
        return null;
    }

    /**
     * 获取请求参数
     *
     * @return {@link Map }<{@link String }, {@link Object }>
     * @since 2.11.0
     */
    public static Map<String, Object> getRequestParams() {
        String body = getRequestBody();
        return CharSequenceUtil.isNotBlank(body) && JSONUtil.isTypeJSON(body)
            ? JSONUtil.toBean(body, Map.class)
            : Collections.unmodifiableMap(JakartaServletUtil.getParamMap(Objects.requireNonNull(getRequest())));
    }

    /**
     * 获取响应状态
     *
     * @return int
     * @since 2.11.0
     */
    public static int getResponseStatus() {
        HttpServletResponse response = getResponse();
        return response != null ? response.getStatus() : -1;
    }

    /**
     * 获取响应所有的头（header）信息
     *
     * @return header值
     * @since 2.11.0
     */
    public static Map<String, String> getResponseHeaders() {
        HttpServletResponse response = getResponse();
        if (response == null) {
            return Collections.emptyMap();
        }
        final Collection<String> headerNames = response.getHeaderNames();
        final Map<String, String> headerMap = MapUtil.newHashMap(headerNames.size(), true);
        for (String name : headerNames) {
            headerMap.put(name, response.getHeader(name));
        }
        return headerMap;
    }

    /**
     * 获取响应 body 参数
     *
     * @return {@link String }
     * @since 2.11.0
     */
    public static String getResponseBody() {
        HttpServletResponse response = getResponse();
        if (response instanceof RepeatReadResponseWrapper wrapper && !wrapper.isStreamingResponse()) {
            String body = wrapper.getResponseContent();
            return JSONUtil.isTypeJSON(body) ? body : null;
        }
        return null;
    }

    /**
     * 获取响应参数
     *
     * @return {@link Map }<{@link String }, {@link Object }>
     * @since 2.11.0
     */
    public static Map<String, Object> getResponseParams() {
        String body = getResponseBody();
        return CharSequenceUtil.isNotBlank(body) && JSONUtil.isTypeJSON(body) ? JSONUtil.toBean(body, Map.class) : null;
    }

    /**
     * 获取 HTTP Session
     *
     * @return HttpSession
     * @since 2.11.0
     */
    public static HttpSession getSession() {
        HttpServletRequest request = getRequest();
        return request != null ? request.getSession() : null;
    }

    /**
     * 获取 HTTP Request
     *
     * @return HttpServletRequest
     * @since 2.11.0
     */
    public static HttpServletRequest getRequest() {
        ServletRequestAttributes attributes = getRequestAttributes();
        if (attributes == null) {
            return null;
        }
        return attributes.getRequest();
    }

    /**
     * 获取 HTTP Response
     *
     * @return HttpServletResponse
     * @since 2.11.0
     */
    public static HttpServletResponse getResponse() {
        ServletRequestAttributes attributes = getRequestAttributes();
        if (attributes == null) {
            return null;
        }
        return attributes.getResponse();
    }

    /**
     * 获取请求属性
     *
     * @return {@link ServletRequestAttributes }
     * @since 2.11.0
     */
    public static ServletRequestAttributes getRequestAttributes() {
        try {
            RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
            return (ServletRequestAttributes)attributes;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 响应 JSON 数据给客户端
     *
     * @param response 响应对象
     * @param data     响应数据
     * @since 2.13.1
     * @see #write(HttpServletResponse, String, String)
     */
    public static void writeJSON(HttpServletResponse response, String data) {
        write(response, data, MediaType.APPLICATION_JSON_VALUE);
    }

    /**
     * 追加查询字符串
     *
     * @param queryString 查询字符串
     * @return {@link StringBuilder }
     */
    private static StringBuilder appendQueryString(String queryString) {
        HttpServletRequest request = getRequest();
        if (request == null) {
            return new StringBuilder();
        }
        return new StringBuilder().append(request.getRequestURL())
            .append(StringConstants.QUESTION_MARK)
            .append(queryString);
    }
    /**
     * 从请求中读取 ip、地址、浏览器、操作系统信息
     */
    public static RequestMeta getRequestMeta() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attrs != null ? attrs.getRequest() : null;
        String ip =  request != null ? JakartaServletUtil.getClientIP(request): null;
        String address = ExceptionUtils.exToNull(() -> IpUtils.getRegion(ip));
        UserAgent ua = request != null ? UserAgentUtil.parse(request.getHeader("User-Agent")) : null;
        String browser = ua != null ? ua.getBrowser().getName() : "Unknown";
        String os = ua != null ? ua.getOs().getName() : "Unknown";
        return new RequestMeta(ip, address, browser, os);
    }

    /**
     * 请求信息
     */
    public record RequestMeta(String ip, String address, String browser, String os) {}
}
