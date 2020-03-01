package com.niroren.common.services;

import org.apache.kafka.streams.KafkaStreams;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class StreamsSupport {

    public static void startStream(KafkaStreams streams) {
        streams.cleanUp();

        final CountDownLatch startLatch = new CountDownLatch(1);
        streams.setStateListener((newState, oldState) -> {
            if (newState == KafkaStreams.State.RUNNING && oldState != KafkaStreams.State.RUNNING) {
                startLatch.countDown();
            }
        });

        streams.start();

        try {
            if (!startLatch.await(60, TimeUnit.SECONDS)) {
                throw new RuntimeException("Streams never finished rebalancing on startup");
            }
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        addShutdownHook(streams);
    }

    private static void addShutdownHook(KafkaStreams streams) {
        Thread.currentThread().setUncaughtExceptionHandler((t, e) -> stopStream(streams));
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                stopStream(streams);
            } catch (final Exception ignored) {
            }
        }));
    }

    private static void stopStream(KafkaStreams streams) {
        if (streams != null) {
            streams.close();
        }
    }
}
