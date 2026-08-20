package com.example.grpcnetty;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {
    private static final int PORT = 8980;
    private static final int PERIOD_SECONDS = 5;

    public static void main(String[] args) throws Exception {
        ExampleServer server = new ExampleServer(PORT);
        server.start();

        ExampleClient client = new ExampleClient("localhost", server.getPort());

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                client.shutdown();
                server.stop();
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
            }
        }));

        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        AtomicInteger tick = new AtomicInteger();

        scheduler.scheduleAtFixedRate(() -> {
            int n = tick.incrementAndGet();
            try {
                client.callSayHello("World-" + n);
                client.callStreaming("Streamer-" + n);
                if (n % 3 == 0) {
                    client.callThrowException("Errorcase-" + n);
                }
            } catch (Exception e) {
                System.out.println("[loop] unexpected exception: " + e);
            }
        }, 0, PERIOD_SECONDS, TimeUnit.SECONDS);

        Thread.currentThread().join();
    }
}
