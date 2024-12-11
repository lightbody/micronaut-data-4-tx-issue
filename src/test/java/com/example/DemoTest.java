package com.example;

import io.micronaut.runtime.EmbeddedApplication;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;

import jakarta.inject.Inject;

@MicronautTest(transactional = false)
class DemoTest {

    @Inject
    EmbeddedApplication<?> application;

    @Inject
    DemoService demoService;

    @Test
    void testItWorks() {
        Assertions.assertTrue(application.isRunning());
        Assertions.assertEquals(1, demoService.foo());
    }

}
