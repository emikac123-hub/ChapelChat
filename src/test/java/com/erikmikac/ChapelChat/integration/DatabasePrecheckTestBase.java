package com.erikmikac.ChapelChat.integration;

import java.net.Socket;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class DatabasePrecheckTestBase {

    @BeforeAll
    void verifyPostgresRunning() {
        String host = "localhost";
        int port = 5432;
        try (Socket socket = new Socket(host, port)) {
            System.out.printf("✅ PostgreSQL is reachable at %s:%d%n", host, port);
        } catch (Exception e) {
            System.err.printf("❌ PostgreSQL is NOT running at %s:%d. Please start it before running tests.%n", host, port);
            throw new IllegalStateException("PostgreSQL not available", e);
        }
    }
}
