package top.wyhao.system.admin.app.service;

import java.util.function.Supplier;

/**
 * 把一次待验证写入放进独立事务。唯一约束失败后，下一次写入不能沿用已中止的事务。
 */
public interface RegistrationTransactions {

    void run(Runnable work);

    <T> T call(Supplier<T> work);
}
