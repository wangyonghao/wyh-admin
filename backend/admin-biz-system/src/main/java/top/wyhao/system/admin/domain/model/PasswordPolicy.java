package top.wyhao.system.admin.domain.model;

/**
 * 当前生效的密码策略快照（纯数据，不含校验逻辑）。
 */
public record PasswordPolicy(
    int minLength,
    int maxLength,
    boolean requireSymbols,
    boolean allowContainUsername,
    int historyRepetitionTimes,
    int expireDays
) {

    public static final int DEFAULT_MAX_LENGTH = 32;

    public static final int MIN_LENGTH_LOWER = 8;
    public static final int MIN_LENGTH_UPPER = 32;
    public static final int REPETITION_TIMES_LOWER = 3;
    public static final int REPETITION_TIMES_UPPER = PasswordHistory.MAX_SIZE;
    public static final int EXPIRE_DAYS_LOWER = 0;
    public static final int EXPIRE_DAYS_UPPER = 999;
    public static final int WARNING_DAYS_LOWER = 0;
    public static final int WARNING_DAYS_UPPER = 998;
    public static final int ERROR_LOCK_COUNT_LOWER = 0;
    public static final int ERROR_LOCK_COUNT_UPPER = 10;
    public static final int ERROR_LOCK_MINUTES_LOWER = 1;
    public static final int ERROR_LOCK_MINUTES_UPPER = 1440;
}
