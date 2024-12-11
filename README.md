This is a simple application that highlights a core change in behavior as we migrate our application from Micronaut 3.x
to 4.x.

The details of our application are:

- Running Micronaut 3.x
- Uses a mixture of Jdbi and Micronaut Data across two data sources (not relevant in this test)
- Uses Micronaut Data Transactions for tx management, but previously used Spring

A key thing to understand it is has a LOT of Jdbi code that wasn't originally wrapped with @Transactional, so to support
this migration we have wrapped the `Jdbi` class such that it tries to detect if it's already in a transaction and, if 
not, it will initiate a new transaction using `TransactionOperations`.

Furthermore, we also have a lot of code that kicks off async event listeners and/or uses `@Async` paired with code that
is configured using the tx synchronizers to execute after commit. We did _not_ utilize `TransactionalEventListener`,
primarily because it didn't exist or work with Spring transactions at the time.

Now that we're migrating, we're finding that code like the flow in this example produce a "connection is closed" error.
It seems that the new `PropagatedContext` system (paired with `ExecutorServiceInstrumenter`) is causing the Hikari 
connection, which was closed when the upstream transaction committed, to be carried forward into the downstream thread.
This results in a "connection is closed" because internally Hikari isn't expecting anyone to use that connection anymore.

I'd like to understand a) why this is happening and b) anything we can do that doesn't involve examining or rewriting 
the existing 300+ usages one of `@Async`, `@EventListener`, and/or our own custom `registerSynchronization` usage.
