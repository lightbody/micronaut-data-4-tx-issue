package com.example;

import io.micronaut.scheduling.TaskExecutors;
import io.micronaut.transaction.TransactionOperations;
import io.micronaut.transaction.support.TransactionSynchronization;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import org.jdbi.v3.core.Jdbi;

import java.sql.Connection;
import java.util.concurrent.ExecutorService;

@Singleton
public class DemoService {
    private final Jdbi jdbi;
    private final TransactionOperations<Connection> transactionManager;
    private final ExecutorService executorService;

    public DemoService(Jdbi jdbi,
                       TransactionOperations<Connection> transactionManager,
                       @Named(TaskExecutors.SCHEDULED) ExecutorService executorService) {
        this.jdbi = jdbi;
        this.transactionManager = transactionManager;
        this.executorService = executorService;
    }

    int foo() {
        // this works
        transactionManager.executeWrite(status -> {
            jdbi.useExtension(FooDao.class, dao -> dao.saveFoo("boop"));
            status.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    executorService.submit(() -> {
                        doThisOnCommit();
                    });
                }
            });
            return true;
        });

        return 1;
    }

    void doThisOnCommit() {
        try {
            // this fails with a "connection is closed" error, but if the method is called inline in afterCommit above (rather than via the executor) it works.
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
