package com.codeinsight.backend.ai;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

@Component
public class ProblemDataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(ProblemDataInitializer.class);

    private final JdbcTemplate jdbcTemplate;
    private boolean vectorSupported = false;

    public ProblemDataInitializer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public boolean isVectorSupported() {
        return vectorSupported;
    }

    @Override
    public void run(String... args) {
        try {
            initPgVector();
            initTable();
            seedProblemsIfEmpty();
        } catch (Exception e) {
            log.warn("[RAG] Initialization notice: {}", e.getMessage());
        }
    }

    private void initPgVector() {
        try {
            jdbcTemplate.execute("CREATE EXTENSION IF NOT EXISTS vector;");
            vectorSupported = true;
            log.info("[RAG/PGVECTOR] Successfully enabled pgvector extension in PostgreSQL!");
        } catch (Exception e) {
            vectorSupported = false;
            log.warn("[RAG/PGVECTOR] pgvector extension not available or not permitted. Falling back to keyword semantic ranking: {}", e.getMessage());
        }
    }

    private void initTable() {
        try {
            jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS coding_problems (
                    id BIGSERIAL PRIMARY KEY,
                    title VARCHAR(255) NOT NULL,
                    slug VARCHAR(255) UNIQUE NOT NULL,
                    platform VARCHAR(50) NOT NULL,
                    difficulty VARCHAR(20) NOT NULL,
                    topic VARCHAR(100) NOT NULL,
                    description TEXT NOT NULL,
                    url VARCHAR(500) NOT NULL,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                );
            """);

            if (vectorSupported) {
                try {
                    jdbcTemplate.execute("ALTER TABLE coding_problems ADD COLUMN IF NOT EXISTS embedding vector(1536);");
                    log.info("[RAG/PGVECTOR] Column 'embedding vector(1536)' verified on coding_problems.");
                } catch (Exception e) {
                    log.warn("[RAG/PGVECTOR] Could not add vector column: {}", e.getMessage());
                }
            }
        } catch (Exception e) {
            log.error("[RAG] Failed to create or update coding_problems table", e);
        }
    }

    private void seedProblemsIfEmpty() {
        try {
            Integer count = jdbcTemplate.queryForObject("SELECT count(*) FROM coding_problems", Integer.class);
            if (count != null && count > 0) {
                log.info("[RAG] coding_problems table already populated with {} problems.", count);
                return;
            }

            log.info("[RAG] Seeding curated DSA interview problem library...");

            List<ProblemSeed> seeds = Arrays.asList(
                // Dynamic Programming
                new ProblemSeed("Climbing Stairs", "climbing-stairs", "LeetCode", "EASY", "Dynamic Programming",
                    "Count ways to reach the top of a staircase where you can climb 1 or 2 steps at a time. Basic fibonacci state transition.", "https://leetcode.com/problems/climbing-stairs/"),
                new ProblemSeed("Coin Change", "coin-change", "LeetCode", "MEDIUM", "Dynamic Programming",
                    "Find fewest number of coins needed to make up a given amount. Classic unbounded knapsack with optimal substructure.", "https://leetcode.com/problems/coin-change/"),
                new ProblemSeed("Longest Increasing Subsequence", "longest-increasing-subsequence", "LeetCode", "MEDIUM", "Dynamic Programming",
                    "Find length of longest strictly increasing subsequence in an integer array using DP or Patience Sort Binary Search.", "https://leetcode.com/problems/longest-increasing-subsequence/"),
                new ProblemSeed("0 - 1 Knapsack Problem", "0-1-knapsack-problem", "GeeksforGeeks", "MEDIUM", "Dynamic Programming",
                    "Given weights and values of N items, put these items in a knapsack of capacity W to get maximum total value in knapsack.", "https://www.geeksforgeeks.org/problems/0-1-knapsack-problem0920/1"),
                new ProblemSeed("Word Break", "word-break", "LeetCode", "MEDIUM", "Dynamic Programming",
                    "Determine if string can be segmented into space-separated sequence of one or more dictionary words using boolean memoization.", "https://leetcode.com/problems/word-break/"),
                new ProblemSeed("House Robber", "house-robber", "LeetCode", "MEDIUM", "Dynamic Programming",
                    "Maximize loot from adjacent houses without alerting police. 1D dynamic programming recurrence with non-adjacent selection.", "https://leetcode.com/problems/house-robber/"),
                new ProblemSeed("Edit Distance", "edit-distance", "LeetCode", "HARD", "Dynamic Programming",
                    "Minimum number of operations to convert word1 to word2 using insert, delete, or replace operations.", "https://leetcode.com/problems/edit-distance/"),

                // Trees & Graphs
                new ProblemSeed("Number of Islands", "number-of-islands", "LeetCode", "MEDIUM", "Graphs",
                    "Count number of connected 1s on a 2D grid using Breadth-First Search (BFS) or Depth-First Search (DFS) or Disjoint Set Union.", "https://leetcode.com/problems/number-of-islands/"),
                new ProblemSeed("Course Schedule", "course-schedule", "LeetCode", "MEDIUM", "Graphs",
                    "Detect cycle in directed dependency graph using topological sort Kahn algorithm or DFS cycle detection.", "https://leetcode.com/problems/course-schedule/"),
                new ProblemSeed("Network Delay Time", "network-delay-time", "LeetCode", "MEDIUM", "Graphs",
                    "Find time it takes for all nodes to receive signal using Dijkstra shortest path algorithm with priority queue.", "https://leetcode.com/problems/network-delay-time/"),
                new ProblemSeed("Clone Graph", "clone-graph", "LeetCode", "MEDIUM", "Graphs",
                    "Return deep copy clone of connected undirected graph using hash map tracking visited nodes with BFS/DFS.", "https://leetcode.com/problems/clone-graph/"),
                new ProblemSeed("Lowest Common Ancestor of a Binary Tree", "lowest-common-ancestor-of-a-binary-tree", "LeetCode", "MEDIUM", "Trees",
                    "Find the lowest node in binary tree that has both p and q as descendants using post-order tree traversal.", "https://leetcode.com/problems/lowest-common-ancestor-of-a-binary-tree/"),
                new ProblemSeed("Binary Tree Level Order Traversal", "binary-tree-level-order-traversal", "LeetCode", "MEDIUM", "Trees",
                    "Return level order traversal of binary tree node values using queue based BFS level by level.", "https://leetcode.com/problems/binary-tree-level-order-traversal/"),
                new ProblemSeed("Maximum Depth of Binary Tree", "maximum-depth-of-binary-tree", "LeetCode", "EASY", "Trees",
                    "Find the number of nodes along the longest path from the root node down to the farthest leaf node.", "https://leetcode.com/problems/maximum-depth-of-binary-tree/"),
                new ProblemSeed("Diameter of Binary Tree", "diameter-of-binary-tree", "LeetCode", "EASY", "Trees",
                    "Length of longest path between any two nodes in a tree, not necessarily passing through the root.", "https://leetcode.com/problems/diameter-of-binary-tree/"),

                // Sliding Window & Two Pointers
                new ProblemSeed("Longest Substring Without Repeating Characters", "longest-substring-without-repeating-characters", "LeetCode", "MEDIUM", "Sliding Window",
                    "Find length of longest substring without duplicate characters using two pointers sliding window and character frequency map.", "https://leetcode.com/problems/longest-substring-without-repeating-characters/"),
                new ProblemSeed("Minimum Window Substring", "minimum-window-substring", "LeetCode", "HARD", "Sliding Window",
                    "Find minimum window in string s containing all characters of pattern t using expanding and shrinking sliding window.", "https://leetcode.com/problems/minimum-window-substring/"),
                new ProblemSeed("3Sum", "3sum", "LeetCode", "MEDIUM", "Two Pointers",
                    "Find all unique triplets in array that sum up to zero using sorting and two pointers opposite ends.", "https://leetcode.com/problems/3sum/"),
                new ProblemSeed("Container With Most Water", "container-with-most-water", "LeetCode", "MEDIUM", "Two Pointers",
                    "Find two lines that together with x-axis form a container holding maximum water area using inward greedy two pointers.", "https://leetcode.com/problems/container-with-most-water/"),
                new ProblemSeed("Trapping Rain Water", "trapping-rain-water", "LeetCode", "HARD", "Two Pointers",
                    "Compute how much water can be trapped after raining using two pointers left-max and right-max boundaries.", "https://leetcode.com/problems/trapping-rain-water/"),

                // Binary Search & Arrays
                new ProblemSeed("Search in Rotated Sorted Array", "search-in-rotated-sorted-array", "LeetCode", "MEDIUM", "Binary Search",
                    "Find index of target in sorted array that has been rotated at unknown pivot using modified binary search on sorted half.", "https://leetcode.com/problems/search-in-rotated-sorted-array/"),
                new ProblemSeed("Find Minimum in Rotated Sorted Array", "find-minimum-in-rotated-sorted-array", "LeetCode", "MEDIUM", "Binary Search",
                    "Locate inflection point where rotation occurred using binary search comparing mid with high element.", "https://leetcode.com/problems/find-minimum-in-rotated-sorted-array/"),
                new ProblemSeed("Koko Eating Bananas", "koko-eating-bananas", "LeetCode", "MEDIUM", "Binary Search",
                    "Find minimum integer eating speed k to eat all bananas within h hours using binary search on answer range.", "https://leetcode.com/problems/koko-eating-bananas/"),

                // Stack & Heap
                new ProblemSeed("Valid Parentheses", "valid-parentheses", "LeetCode", "EASY", "Stack",
                    "Determine if input string brackets are valid and closed in correct order using LIFO stack data structure.", "https://leetcode.com/problems/valid-parentheses/"),
                new ProblemSeed("Daily Temperatures", "daily-temperatures", "LeetCode", "MEDIUM", "Stack",
                    "Calculate number of days to wait until a warmer temperature using monotonic decreasing stack storing indices.", "https://leetcode.com/problems/daily-temperatures/"),
                new ProblemSeed("Top K Frequent Elements", "top-k-frequent-elements", "LeetCode", "MEDIUM", "Heap",
                    "Return k most frequent elements in array using min-heap of size k or bucket sort frequency map.", "https://leetcode.com/problems/top-k-frequent-elements/"),
                new ProblemSeed("Merge k Sorted Lists", "merge-k-sorted-lists", "LeetCode", "HARD", "Heap",
                    "Merge k sorted linked lists into one sorted linked list using min-priority queue comparing head nodes.", "https://leetcode.com/problems/merge-k-sorted-lists/"),

                // Backtracking
                new ProblemSeed("Subsets", "subsets", "LeetCode", "MEDIUM", "Backtracking",
                    "Generate all possible subsets (power set) of unique elements using recursive backtracking inclusion-exclusion.", "https://leetcode.com/problems/subsets/"),
                new ProblemSeed("Permutations", "permutations", "LeetCode", "MEDIUM", "Backtracking",
                    "Return all possible permutations of distinct integers using backtracking recursion with swap or visited tracking.", "https://leetcode.com/problems/permutations/"),
                new ProblemSeed("Combination Sum", "combination-sum", "LeetCode", "MEDIUM", "Backtracking",
                    "Find all unique combinations in candidates where candidate numbers sum to target using backtracking with unbounded reuse.", "https://leetcode.com/problems/combination-sum/"),
                new ProblemSeed("Word Search", "word-search", "LeetCode", "MEDIUM", "Backtracking",
                    "Check if word exists in grid of characters using DFS 2D grid backtracking with cell mark unmark.", "https://leetcode.com/problems/word-search/")
            );

            for (ProblemSeed p : seeds) {
                String vectorStr = generateVectorString(p.title + " " + p.topic + " " + p.description);
                if (vectorSupported) {
                    jdbcTemplate.update("""
                        INSERT INTO coding_problems (title, slug, platform, difficulty, topic, description, url, embedding)
                        VALUES (?, ?, ?, ?, ?, ?, ?, CAST(? AS vector))
                        ON CONFLICT (slug) DO NOTHING
                    """, p.title, p.slug, p.platform, p.difficulty, p.topic, p.description, p.url, vectorStr);
                } else {
                    jdbcTemplate.update("""
                        INSERT INTO coding_problems (title, slug, platform, difficulty, topic, description, url)
                        VALUES (?, ?, ?, ?, ?, ?, ?)
                        ON CONFLICT (slug) DO NOTHING
                    """, p.title, p.slug, p.platform, p.difficulty, p.topic, p.description, p.url);
                }
            }

            log.info("[RAG] Successfully seeded {} high-yield DSA interview problems into coding_problems!", seeds.size());
        } catch (Exception e) {
            log.error("[RAG] Failed to seed problem library", e);
        }
    }

    /**
     * Deterministically generates a normalized unit vector of dimension 1536 based on text features
     */
    public static String generateVectorString(String text) {
        float[] vector = new float[1536];
        long seed = text.toLowerCase().hashCode();
        Random rng = new Random(seed);

        float normSq = 0f;
        for (int i = 0; i < 1536; i++) {
            vector[i] = (float) rng.nextGaussian();
            normSq += vector[i] * vector[i];
        }

        // Project semantic topic weights into specific vector segments
        String lower = text.toLowerCase();
        int offset = 0;
        if (lower.contains("dynamic programming") || lower.contains("dp") || lower.contains("knapsack")) offset = 100;
        else if (lower.contains("graph") || lower.contains("bfs") || lower.contains("dfs") || lower.contains("dijkstra")) offset = 300;
        else if (lower.contains("tree") || lower.contains("traversal") || lower.contains("lca")) offset = 500;
        else if (lower.contains("sliding window") || lower.contains("two pointers")) offset = 700;
        else if (lower.contains("binary search")) offset = 900;
        else if (lower.contains("stack") || lower.contains("heap") || lower.contains("priority queue")) offset = 1100;
        else if (lower.contains("backtracking") || lower.contains("permutation")) offset = 1300;

        for (int i = 0; i < 50; i++) {
            vector[offset + i] += 2.5f;
            normSq += 2.5f * 2.5f;
        }

        float norm = (float) Math.sqrt(normSq);
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < 1536; i++) {
            if (i > 0) sb.append(",");
            sb.append(String.format(java.util.Locale.US, "%.6f", vector[i] / norm));
        }
        sb.append("]");
        return sb.toString();
    }

    private static class ProblemSeed {
        String title, slug, platform, difficulty, topic, description, url;
        ProblemSeed(String title, String slug, String platform, String difficulty, String topic, String description, String url) {
            this.title = title;
            this.slug = slug;
            this.platform = platform;
            this.difficulty = difficulty;
            this.topic = topic;
            this.description = description;
            this.url = url;
        }
    }
}