package top.wyhao.system.admin.app.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.quartz.CronExpression;
import top.wyhao.system.admin.app.dto.ScheduleSpec;
import top.wyhao.system.admin.domain.exception.JobException;

import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * 把白话调度配置编译成 Quartz Cron 与中文说明。
 */
public final class CronComposer {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final String[] WEEKDAY_CRON = {"MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN"};
    private static final String[] WEEKDAY_LABEL = {"周一", "周二", "周三", "周四", "周五", "周六", "周日"};
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private CronComposer() {
    }

    public static Compiled compile(ScheduleSpec spec) {
        if (spec == null || spec.getMode() == null) {
            throw JobException.scheduleModeRequired();
        }
        String mode = spec.getMode().trim().toUpperCase(Locale.ROOT);
        String cron;
        String label;
        switch (mode) {
            case "DAILY" -> {
                int hour = requireHour(spec.getHour());
                int minute = requireMinute(spec.getMinute());
                cron = "0 %d %d * * ?".formatted(minute, hour);
                label = "每天 %s".formatted(clock(hour, minute));
            }
            case "WEEKLY" -> {
                int hour = requireHour(spec.getHour());
                int minute = requireMinute(spec.getMinute());
                List<Integer> days = spec.getDaysOfWeek();
                if (days == null || days.isEmpty()) {
                    throw JobException.weekdayRequired();
                }
                List<Integer> sorted = days.stream().distinct().sorted().toList();
                for (Integer day : sorted) {
                    if (day == null || day < 1 || day > 7) {
                        throw JobException.weekdayInvalid();
                    }
                }
                String cronDays = sorted.stream()
                        .map(day -> WEEKDAY_CRON[day - 1])
                        .collect(Collectors.joining(","));
                String labelDays = sorted.stream()
                        .map(day -> WEEKDAY_LABEL[day - 1])
                        .collect(Collectors.joining("、"));
                cron = "0 %d %d ? * %s".formatted(minute, hour, cronDays);
                label = "每%s %s".formatted(labelDays, clock(hour, minute));
            }
            case "MONTHLY" -> {
                int hour = requireHour(spec.getHour());
                int minute = requireMinute(spec.getMinute());
                Integer day = spec.getDayOfMonth();
                if (day == null || day < 1 || day > 31) {
                    throw JobException.dayOfMonthRequired();
                }
                cron = "0 %d %d %d * ?".formatted(minute, hour, day);
                label = "每月 %d 号 %s".formatted(day, clock(hour, minute));
            }
            case "INTERVAL" -> {
                Integer interval = spec.getInterval();
                if (interval == null || interval < 1) {
                    throw JobException.intervalInvalid();
                }
                String unit = spec.getIntervalUnit() == null ? "MINUTE" : spec.getIntervalUnit().toUpperCase(Locale.ROOT);
                if ("HOUR".equals(unit)) {
                    cron = "0 0 */%d * * ?".formatted(interval);
                    label = interval == 1 ? "每小时" : "每隔 %d 小时".formatted(interval);
                } else {
                    cron = "0 0/%d * * * ?".formatted(interval);
                    label = interval == 1 ? "每分钟" : "每隔 %d 分钟".formatted(interval);
                }
            }
            case "CRON" -> {
                if (spec.getCron() == null || spec.getCron().isBlank()) {
                    throw JobException.cronRequired();
                }
                cron = spec.getCron().trim();
                if (!CronExpression.isValidExpression(cron)) {
                    throw JobException.cronInvalid();
                }
                label = "自定义：" + cron;
            }
            default -> throw JobException.scheduleModeUnsupported();
        }
        if (!CronExpression.isValidExpression(cron)) {
            throw JobException.generatedCronInvalid();
        }
        spec.setMode(mode);
        spec.setCron(cron);
        return new Compiled(cron, label, writeJson(spec));
    }

    public static ScheduleSpec parse(String payloadJson) {
        if (payloadJson == null || payloadJson.isBlank()) {
            return null;
        }
        try {
            return MAPPER.readValue(payloadJson, ScheduleSpec.class);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    public static List<String> upcoming(String cron, int count) {
        List<String> times = new ArrayList<>();
        try {
            CronExpression expression = new CronExpression(cron);
            Date cursor = new Date();
            ZoneId zone = ZoneId.systemDefault();
            for (int i = 0; i < count; i++) {
                Date next = expression.getNextValidTimeAfter(cursor);
                if (next == null) {
                    break;
                }
                times.add(LocalDateTime.ofInstant(next.toInstant(), zone).format(TIME_FMT));
                cursor = next;
            }
        } catch (ParseException ignored) {
            return times;
        }
        return times;
    }

    private static int requireHour(Integer hour) {
        if (hour == null || hour < 0 || hour > 23) {
            throw JobException.hourRequired();
        }
        return hour;
    }

    private static int requireMinute(Integer minute) {
        if (minute == null || minute < 0 || minute > 59) {
            throw JobException.minuteRequired();
        }
        return minute;
    }

    private static String clock(int hour, int minute) {
        return "%02d:%02d".formatted(hour, minute);
    }

    private static String writeJson(ScheduleSpec spec) {
        try {
            return MAPPER.writeValueAsString(spec);
        } catch (JsonProcessingException e) {
            throw JobException.scheduleSerializeFailed();
        }
    }

    public record Compiled(String cron, String label, String payloadJson) {
    }
}
