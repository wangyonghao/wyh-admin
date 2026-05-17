package top.wyhao.admin.system.otp.service;

/**
 * 限流器接口
 *
 * @author wyhao
 */
public interface RateLimiter {

    /**
     * 检查是否允许请求
     *
     * @param key    限流键
     * @param max    最大次数
     * @param window 时间窗口（秒）
     * @return 是否允许
     */
    boolean isAllowed(String key, int max, int window);

    /**
     * 增加计数
     *
     * @param key    限流键
     * @param window 时间窗口（秒）
     * @return 当前计数
     */
    long increment(String key, int window);

    /**
     * 获取当前计数
     *
     * @param key 限流键
     * @return 当前计数
     */
    long getCount(String key);

    /**
     * 删除限流键
     *
     * @param key 限流键
     */
    void delete(String key);
}
