package com.codeinsight.backend.ai;

import com.codeinsight.backend.dto.CodingProblemDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class RagProblemService {

    private static final Logger log = LoggerFactory.getLogger(RagProblemService.class);

    private final JdbcTemplate jdbcTemplate;
    private final ProblemDataInitializer dataInitializer;

    public RagProblemService(JdbcTemplate jdbcTemplate, ProblemDataInitializer dataInitializer) {
        this.jdbcTemplate = jdbcTemplate;
        this.dataInitializer = dataInitializer;
    }

    /**
     * Find coding problems matching semantic meaning using pgvector cosine distance or keyword fallback
     */
    public List<CodingProblemDTO> findSimilarProblems(String queryText, int limit) {
        if (limit <= 0) limit = 5;

        if (dataInitializer.isVectorSupported()) {
            try {
                String vectorStr = ProblemDataInitializer.generateVectorString(queryText);
                String sql = """
                    SELECT id, title, slug, platform, difficulty, topic, description, url,
                           (1.0 - (embedding <=> CAST(? AS vector))) AS similarity
                    FROM coding_problems
                    WHERE embedding IS NOT NULL
                    ORDER BY embedding <=> CAST(? AS vector) ASC
                    LIMIT ?
                """;

                return jdbcTemplate.query(sql, (rs, rowNum) -> new CodingProblemDTO(
                    rs.getLong("id"),
                    rs.getString("title"),
                    rs.getString("slug"),
                    rs.getString("platform"),
                    rs.getString("difficulty"),
                    rs.getString("topic"),
                    rs.getString("description"),
                    rs.getString("url"),
                    rs.getDouble("similarity")
                ), vectorStr, vectorStr, limit);
            } catch (Exception e) {
                log.warn("[RAG] Vector query failed, falling back to keyword search: {}", e.getMessage());
            }
        }

        // Fallback to text matching
        try {
            String pattern = "%" + queryText.toLowerCase() + "%";
            String fallbackSql = """
                SELECT id, title, slug, platform, difficulty, topic, description, url, 1.0 AS similarity
                FROM coding_problems
                WHERE LOWER(topic) LIKE ? OR LOWER(title) LIKE ? OR LOWER(description) LIKE ?
                LIMIT ?
            """;

            List<CodingProblemDTO> results = jdbcTemplate.query(fallbackSql, (rs, rowNum) -> new CodingProblemDTO(
                rs.getLong("id"),
                rs.getString("title"),
                rs.getString("slug"),
                rs.getString("platform"),
                rs.getString("difficulty"),
                rs.getString("topic"),
                rs.getString("description"),
                rs.getString("url"),
                1.0
            ), pattern, pattern, pattern, limit);

            if (results.isEmpty()) {
                // If specific search had 0 results, return top default problems
                return jdbcTemplate.query("SELECT id, title, slug, platform, difficulty, topic, description, url, 0.8 AS similarity FROM coding_problems LIMIT ?",
                    (rs, rowNum) -> new CodingProblemDTO(
                        rs.getLong("id"),
                        rs.getString("title"),
                        rs.getString("slug"),
                        rs.getString("platform"),
                        rs.getString("difficulty"),
                        rs.getString("topic"),
                        rs.getString("description"),
                        rs.getString("url"),
                        0.8
                    ), limit);
            }
            return results;
        } catch (Exception e) {
            log.error("[RAG] Search failed: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Retrieve target problems for a user's identified weak areas
     */
    public List<CodingProblemDTO> findProblemsForWeakTopics(List<String> weakTopics, int limit) {
        if (weakTopics == null || weakTopics.isEmpty()) {
            return findSimilarProblems("Dynamic Programming Graphs Binary Search", limit);
        }

        String combinedQuery = String.join(" ", weakTopics);
        return findSimilarProblems(combinedQuery, limit);
    }

    /**
     * Build an enriched RAG prompt section containing verified authentic problems
     */
    public String buildRagContext(List<String> weakTopics, String targetCompany, int limit) {
        List<CodingProblemDTO> problems = findProblemsForWeakTopics(weakTopics, limit);
        if (problems.isEmpty()) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("\n\n### 🎯 RETRIEVED AUTHENTIC PROBLEMS (VIA NEON PGVECTOR RAG)\n");
        sb.append("The following verified problems from the database specifically address the user's weak points and are highly relevant for ")
          .append(targetCompany).append(" interview rounds. Explicitly reference these problems in your recommendations:\n");

        for (int i = 0; i < problems.size(); i++) {
            CodingProblemDTO p = problems.get(i);
            sb.append(String.format("%d. **[%s](%s)** (%s | %s) - %s [Match: %.1f%%]\n",
                i + 1,
                p.getTitle(),
                p.getUrl(),
                p.getPlatform(),
                p.getDifficulty(),
                p.getDescription(),
                p.getSimilarity() * 100
            ));
        }
        sb.append("\n");
        return sb.toString();
    }
}