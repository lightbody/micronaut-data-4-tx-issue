package com.example;

import io.micronaut.context.event.ApplicationEventPublisher;
import io.micronaut.runtime.event.annotation.EventListener;
import io.micronaut.scheduling.annotation.Async;
import io.micronaut.transaction.TransactionOperations;
import io.micronaut.transaction.annotation.Transactional;
import io.micronaut.transaction.annotation.TransactionalEventListener;
import jakarta.inject.Singleton;
import org.jdbi.v3.core.Jdbi;

import java.sql.Connection;

@Singleton
public class DemoService {
    private final Jdbi jdbi;
    private final TransactionOperations<Connection> transactionManager;
    private final ApplicationEventPublisher<DumbEvent> eventPublisher;

    public DemoService(Jdbi jdbi,
                       TransactionOperations<Connection> transactionManager,
                       ApplicationEventPublisher<DumbEvent> eventPublisher) {
        this.jdbi = jdbi;
        this.transactionManager = transactionManager;
        this.eventPublisher = eventPublisher;
    }

    int foo() {
        // this works
        transactionManager.executeWrite(status -> {
            jdbi.useExtension(FooDao.class, dao -> dao.saveFoo("boop"));
            eventPublisher.publishEventAsync(new DumbEvent());
            return true;
        });

        return 1;
    }

    @EventListener
    void onEvent(DumbEvent event) {
        try {
            // this fails with a "connection is closed" error; the error does not happen if we do @TransactionalEventListener
            transactionManager.executeWrite(status -> {
                jdbi.useExtension(FooDao.class, dao -> dao.saveFoo("boop"));
                return true;
            });
            System.out.println("we never get here :(");
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
    }
}
