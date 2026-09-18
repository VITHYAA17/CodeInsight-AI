package com.codeinsight.backend.security;

import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RateLimitService {

    private final Map<String, Bucket> bucketCache = new ConcurrentHashMap<>();

    /**
     * Attempts to consume 1 token for the specified key and plan.
     * Returns a ConsumptionProbe containing remaining tokens, wait time, etc.
     */
    public ConsumptionProbe tryConsume(String clientKey, RateLimitPlan plan) {
        String cacheKey = clientKey + ":" + plan.name();
        Bucket bucket = bucketCache.computeIfAbsent(cacheKey, k -> createNewBucket(plan));
        return bucket.tryConsumeAndReturnRemaining(1);
    }

    private Bucket createNewBucket(RateLimitPlan plan) {
        return Bucket.builder()
                .addLimit(plan.getBandwidth())
                .build();
    }

    /**
     * Resets bucket for testing or manual administrative override
     */
    public void resetBucket(String clientKey, RateLimitPlan plan) {
        bucketCache.remove(clientKey + ":" + plan.name());
    }

    public void clearAllBuckets() {
        bucketCache.clear();
    }
}
