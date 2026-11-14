package top.wyhao.system.admin.app.service;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class SystemTimeSource implements TimeSource {

    @Override
    public LocalDateTime now() {
        return LocalDateTime.now();
    }
}
