package top.wyhao.system.admin.app.service;

import java.time.LocalDateTime;

/**
 * 注册有效期使用的时钟。
 */
public interface TimeSource {

    LocalDateTime now();
}
