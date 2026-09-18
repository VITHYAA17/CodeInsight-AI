package com.codeinsight.backend.security;

import io.github.bucket4j.Bandwidth;

import java.time.Duration;

public enum RateLimitPlan {
    AI(10, Duration.ofMinutes(1)),
    SYNC(5, Duration.ofMinutes(5)),
    AUTH(10, Duration.ofMinutes(1)),
    DEFAULT(100, Duration.ofMinutes(1));

    private final long capacity;
    private final Duration duration;

    RateLimitPlan(long capacity, Duration duration) {
        this.capacity = capacity;
        this.duration = duration;
    }

    public long getCapacity() {
        return capacity;
    }

    public Duration getDuration() {
        return duration;
    }

    public Bandwidth getBandwidth() {
        return Bandwidth.builder()
                .capacity(capacity)
                .refillIntervally(capacity, duration)
                .build();
    }
}
