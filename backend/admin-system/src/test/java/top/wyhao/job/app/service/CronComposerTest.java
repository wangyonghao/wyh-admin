package top.wyhao.job.app.service;

import org.junit.jupiter.api.Test;
import top.wyhao.system.admin.app.dto.ScheduleSpec;
import top.wyhao.system.admin.app.service.CronComposer;
import top.wyhao.system.admin.domain.exception.JobException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * {@link CronComposer} 示范单测：白话调度 → Quartz Cron。
 * 断言用 {@code getCode()}/{@code getDefaultMessage()}，避免 {@code getMessage()} 触发 I18n（需 Spring）。
 */
class CronComposerTest {

    @Test
    void shouldCompileDaily() {
        ScheduleSpec spec = spec("DAILY", 9, 30);

        CronComposer.Compiled compiled = CronComposer.compile(spec);

        assertThat(compiled.cron()).isEqualTo("0 30 9 * * ?");
        assertThat(compiled.label()).isEqualTo("每天 09:30");
        assertThat(compiled.payloadJson()).contains("\"mode\":\"DAILY\"");
        assertThat(spec.getCron()).isEqualTo("0 30 9 * * ?");
    }

    @Test
    void shouldCompileWeeklySortedDistinct() {
        ScheduleSpec spec = spec("WEEKLY", 8, 0);
        spec.setDaysOfWeek(List.of(5, 1, 1, 3));

        CronComposer.Compiled compiled = CronComposer.compile(spec);

        assertThat(compiled.cron()).isEqualTo("0 0 8 ? * MON,WED,FRI");
        assertThat(compiled.label()).isEqualTo("每周一、周三、周五 08:00");
    }

    @Test
    void shouldCompileMonthly() {
        ScheduleSpec spec = spec("MONTHLY", 12, 5);
        spec.setDayOfMonth(15);

        CronComposer.Compiled compiled = CronComposer.compile(spec);

        assertThat(compiled.cron()).isEqualTo("0 5 12 15 * ?");
        assertThat(compiled.label()).isEqualTo("每月 15 号 12:05");
    }

    @Test
    void shouldCompileIntervalMinuteAndHour() {
        ScheduleSpec minute = new ScheduleSpec();
        minute.setMode("INTERVAL");
        minute.setInterval(5);
        minute.setIntervalUnit("MINUTE");
        assertThat(CronComposer.compile(minute).cron()).isEqualTo("0 0/5 * * * ?");
        assertThat(CronComposer.compile(minute).label()).isEqualTo("每隔 5 分钟");

        ScheduleSpec hour = new ScheduleSpec();
        hour.setMode("INTERVAL");
        hour.setInterval(1);
        hour.setIntervalUnit("HOUR");
        assertThat(CronComposer.compile(hour).cron()).isEqualTo("0 0 */1 * * ?");
        assertThat(CronComposer.compile(hour).label()).isEqualTo("每小时");
    }

    @Test
    void shouldCompileCustomCron() {
        ScheduleSpec spec = new ScheduleSpec();
        spec.setMode("CRON");
        spec.setCron("0 0 12 * * ?");

        CronComposer.Compiled compiled = CronComposer.compile(spec);

        assertThat(compiled.cron()).isEqualTo("0 0 12 * * ?");
        assertThat(compiled.label()).isEqualTo("自定义：0 0 12 * * ?");
    }

    @Test
    void shouldRejectNullOrUnknownMode() {
        assertThatThrownBy(() -> CronComposer.compile(null))
                .isInstanceOf(JobException.class)
                .extracting(ex -> ((JobException) ex).getCode())
                .isEqualTo("JOB_SCHEDULE_MODE_REQUIRED");

        ScheduleSpec spec = new ScheduleSpec();
        spec.setMode("YEARLY");
        assertThatThrownBy(() -> CronComposer.compile(spec))
                .isInstanceOf(JobException.class)
                .extracting(ex -> ((JobException) ex).getCode())
                .isEqualTo("JOB_SCHEDULE_MODE_UNSUPPORTED");
    }

    @Test
    void shouldRejectInvalidDailyHour() {
        ScheduleSpec spec = spec("DAILY", 24, 0);
        assertThatThrownBy(() -> CronComposer.compile(spec))
                .isInstanceOf(JobException.class)
                .extracting(ex -> ((JobException) ex).getCode())
                .isEqualTo("JOB_HOUR_REQUIRED");
    }

    @Test
    void shouldRejectEmptyWeeklyDays() {
        ScheduleSpec spec = spec("WEEKLY", 9, 0);
        assertThatThrownBy(() -> CronComposer.compile(spec))
                .isInstanceOf(JobException.class)
                .extracting(ex -> ((JobException) ex).getCode())
                .isEqualTo("JOB_WEEKDAY_REQUIRED");
    }

    @Test
    void shouldRejectInvalidCronExpression() {
        ScheduleSpec spec = new ScheduleSpec();
        spec.setMode("CRON");
        spec.setCron("not-a-cron");
        assertThatThrownBy(() -> CronComposer.compile(spec))
                .isInstanceOf(JobException.class)
                .extracting(ex -> ((JobException) ex).getCode())
                .isEqualTo("JOB_CRON_INVALID");
    }

    @Test
    void shouldParseAndUpcoming() {
        assertThat(CronComposer.parse(null)).isNull();
        assertThat(CronComposer.parse("{bad")).isNull();

        ScheduleSpec parsed = CronComposer.parse("{\"mode\":\"DAILY\",\"hour\":1,\"minute\":2}");
        assertThat(parsed).isNotNull();
        assertThat(parsed.getMode()).isEqualTo("DAILY");

        List<String> times = CronComposer.upcoming("0 0 12 * * ?", 3);
        assertThat(times).hasSize(3);
    }

    private static ScheduleSpec spec(String mode, int hour, int minute) {
        ScheduleSpec spec = new ScheduleSpec();
        spec.setMode(mode);
        spec.setHour(hour);
        spec.setMinute(minute);
        return spec;
    }
}
