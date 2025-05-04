package com.example.danmarkskort;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;

public class AppTest {
    Thread thread;


    //region Test setup
    @BeforeEach
    protected void beforeEachTest() {
        thread = new Thread(() ->
            App.main(new String[0])
        );
        thread.start();
    }

    @AfterEach
    protected void afterEachTest() {
        //...
    }
    //endregion

    //region Tests
    @Test
    protected void testTest() throws InterruptedException {
        thread.join();
    }
    //endregion
}