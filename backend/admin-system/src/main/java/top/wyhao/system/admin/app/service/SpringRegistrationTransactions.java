package top.wyhao.system.admin.app.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.function.Supplier;

/**
 * 每次调用使用 REQUIRES_NEW，避免唯一约束把外层事务标成只能回滚。
 */
@Component
@RequiredArgsConstructor
public class SpringRegistrationTransactions implements RegistrationTransactions {

    private final PlatformTransactionManager transactionManager;

    @Override
    public void run(Runnable work) {
        template().executeWithoutResult(status -> work.run());
    }

    @Override
    public <T> T call(Supplier<T> work) {
        return template().execute(status -> work.get());
    }

    private TransactionTemplate template() {
        TransactionTemplate template = new TransactionTemplate(transactionManager);
        template.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        return template;
    }
}
