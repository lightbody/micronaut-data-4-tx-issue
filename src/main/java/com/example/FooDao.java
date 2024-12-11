package com.example;

import org.jdbi.v3.sqlobject.statement.SqlUpdate;

public interface FooDao {
    @SqlUpdate("insert into foo(foo) values (:foo)")
    void saveFoo(String foo);
}
