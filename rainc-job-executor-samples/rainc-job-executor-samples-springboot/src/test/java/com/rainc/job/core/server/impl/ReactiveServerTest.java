package com.rainc.job.core.server.impl;

import org.junit.Test;

import java.io.IOException;

/**
 * @Author rainc
 * @create 2020/12/9 22:34
 */
public class ReactiveServerTest {
    @Test
    public void testStart() {
        ReactiveServer reactiveServer = new ReactiveServer();
        reactiveServer.start("aa",9999,"bb",null);
        try {
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
