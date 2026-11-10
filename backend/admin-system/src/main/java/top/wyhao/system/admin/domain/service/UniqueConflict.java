package top.wyhao.system.admin.domain.service;

/**
 * 识别用户表唯一约束冲突。只看异常链里的约束名，不读取密码。
 */
public final class UniqueConflict {

    private UniqueConflict() {
    }

    public static boolean targetsEmail(Throwable error) {
        return text(error).contains("uk_user_email");
    }

    public static boolean targetsUsername(Throwable error) {
        return text(error).contains("uk_user_username");
    }

    private static String text(Throwable error) {
        StringBuilder builder = new StringBuilder();
        Throwable current = error;
        int depth = 0;
        while (current != null && depth < 8) {
            if (current.getMessage() != null) {
                builder.append(current.getMessage()).append('\n');
            }
            current = current.getCause();
            depth++;
        }
        return builder.toString();
    }
}
