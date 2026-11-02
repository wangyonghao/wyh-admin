package top.wyhao.system.admin.domain.service;

import cn.hutool.core.collection.CollUtil;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import top.wyhao.cmn.core.enums.BaseEnum;

import java.util.Collection;
import java.util.List;

/**
 * 用户导入重复策略及判定。
 */
@Component
public class UserImportPolicy {

    /**
     * 重复数据处理策略。
     */
    @Getter
    @RequiredArgsConstructor
    public enum Strategy implements BaseEnum {

        /** 跳过该行 */
        SKIP(1, "跳过该行"),

        /** 修改数据 */
        UPDATE(2, "修改数据"),

        /** 停止导入 */
        EXIT(3, "停止导入");

        private final Integer value;
        private final String description;
    }

    /**
     * 导入行联系方式。
     */
    public record ContactRow(String username, String email, String phone) {
    }

    public boolean shouldSkip(Strategy duplicateUser,
                              Strategy duplicateEmail,
                              Strategy duplicatePhone,
                              String username,
                              String email,
                              String phone,
                              Collection<String> existUsernames,
                              Collection<String> existEmails,
                              Collection<String> existPhones) {
        return matches(Strategy.SKIP, duplicateUser, username, existUsernames)
                || matches(Strategy.SKIP, duplicateEmail, email, existEmails)
                || matches(Strategy.SKIP, duplicatePhone, phone, existPhones);
    }

    public boolean shouldAbort(Strategy duplicateUser,
                               Strategy duplicateEmail,
                               Strategy duplicatePhone,
                               List<ContactRow> rows,
                               Collection<String> existUsernames,
                               Collection<String> existEmails,
                               Collection<String> existPhones) {
        return rows.stream().anyMatch(row ->
                matches(Strategy.EXIT, duplicateUser, row.username(), existUsernames)
                        || matches(Strategy.EXIT, duplicateEmail, row.email(), existEmails)
                        || matches(Strategy.EXIT, duplicatePhone, row.phone(), existPhones));
    }

    public boolean shouldUpdateExistingUser(Strategy duplicateUser,
                                            String username,
                                            Collection<String> existUsernames) {
        return matches(Strategy.UPDATE, duplicateUser, username, existUsernames);
    }

    private static boolean matches(Strategy expected,
                                   Strategy actual,
                                   String data,
                                   Collection<String> existList) {
        return expected == actual && CollUtil.isNotEmpty(existList) && existList.contains(data);
    }
}
