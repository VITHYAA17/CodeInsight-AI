package com.codeinsight.backend.integration;

import com.codeinsight.backend.dto.StatisticsDTO;

public interface PlatformService {
    
    /**
     * Connect a user's coding account on this platform
     */
    void connectAccount(Long userId, String username);
    
    /**
     * Fetch and store user's profile data from the platform
     */
    void syncUserData(Long userId, String username);
    
    /**
     * Get cached statistics for a user
     */
    StatisticsDTO getUserStatistics(Long userId);
    
    /**
     * Get platform name (leetcode, codeforces, etc.)
     */
    String getPlatformName();
}
