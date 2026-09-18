package com.codeinsight.backend.security;

import io.github.bucket4j.ConsumptionProbe;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RateLimitServiceTest {

    private RateLimitService rateLimitService;

    @BeforeEach
    void setUp() {
        rateLimitService = new RateLimitService();
    }

    @Test
    @DisplayName("Should allow requests within capacity and decrement remaining tokens")
    void shouldAllowRequestsWithinCapacity() {
        String clientKey = "test-user-1";
        RateLimitPlan plan = RateLimitPlan.AI; // Capacity 10

        ConsumptionProbe probe1 = rateLimitService.tryConsume(clientKey, plan);
        assertTrue(probe1.isConsumed(), "First request should be consumed");
        assertEquals(9, probe1.getRemainingTokens(), "Remaining tokens should be 9");

        ConsumptionProbe probe2 = rateLimitService.tryConsume(clientKey, plan);
        assertTrue(probe2.isConsumed(), "Second request should be consumed");
        assertEquals(8, probe2.getRemainingTokens(), "Remaining tokens should be 8");
    }

    @Test
    @DisplayName("Should reject requests when token bucket capacity is exceeded")
    void shouldRejectWhenCapacityExceeded() {
        String clientKey = "test-user-sync";
        RateLimitPlan plan = RateLimitPlan.SYNC; // Capacity 5

        for (int i = 0; i < 5; i++) {
            ConsumptionProbe probe = rateLimitService.tryConsume(clientKey, plan);
            assertTrue(probe.isConsumed(), "Request " + (i + 1) + " should succeed");
        }

        // 6th request should fail
        ConsumptionProbe rejectedProbe = rateLimitService.tryConsume(clientKey, plan);
        assertFalse(rejectedProbe.isConsumed(), "6th request should be rejected");
        assertEquals(0, rejectedProbe.getRemainingTokens(), "Remaining tokens should be 0");
        assertTrue(rejectedProbe.getNanosToWaitForRefill() > 0, "Wait time should be greater than 0");
    }

    @Test
    @DisplayName("Should isolate rate limits between different clients and plans")
    void shouldIsolateBetweenClients() {
        String clientA = "user-a";
        String clientB = "user-b";
        RateLimitPlan plan = RateLimitPlan.AUTH; // Capacity 10

        for (int i = 0; i < 10; i++) {
            rateLimitService.tryConsume(clientA, plan);
        }

        // Client A should be exhausted
        assertFalse(rateLimitService.tryConsume(clientA, plan).isConsumed());

        // Client B should still have full capacity
        ConsumptionProbe probeB = rateLimitService.tryConsume(clientB, plan);
        assertTrue(probeB.isConsumed(), "Client B should not be affected by Client A");
        assertEquals(9, probeB.getRemainingTokens());
    }
}
