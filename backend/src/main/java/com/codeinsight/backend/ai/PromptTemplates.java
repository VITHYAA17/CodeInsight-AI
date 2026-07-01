package com.codeinsight.backend.ai;

import org.springframework.stereotype.Component;

@Component
public class PromptTemplates {

    public static String getRecommendationPrompt(String userProfile, String targetCompany) {
        return """
                You are an expert coding interview coach. Based on the user's profile below, generate specific interview preparation recommendations.
                
                USER PROFILE:
                %s
                
                TARGET COMPANY: %s
                
                Provide:
                1. Top 3 areas to focus on
                2. Estimated time to prepare (in weeks)
                3. Specific topics to master
                4. Practice problem suggestions
                5. Daily schedule recommendation
                
                Format the response clearly with headers and bullet points. Be specific and actionable.
                """.formatted(userProfile, targetCompany);
    }

    public static String getStudyPlanPrompt(String userProfile, String targetCompany, Integer weeksAvailable) {
        return """
                You are an expert coding interview preparation coach. Create a detailed %d-week study plan for a candidate preparing for %s interviews.
                
                USER PROFILE:
                %s
                
                Generate a week-by-week study plan with:
                - Weekly focus topics
                - Number of problems to solve per difficulty
                - Key concepts to understand
                - Estimated hours per week
                - Practice problem recommendations
                - Mock interview schedule
                
                Format each week clearly. Be specific about which topics and patterns to focus on.
                """.formatted(weeksAvailable, targetCompany, userProfile);
    }

    public static String getTopicGuidancePrompt(String topic, String currentLevel, String targetLevel) {
        return """
                You are a DSA (Data Structures & Algorithms) expert. Create a learning guide for mastering %s.
                
                CURRENT LEVEL: %s
                TARGET LEVEL: %s
                
                Provide:
                1. Key concepts to understand
                2. Common patterns and techniques
                3. Step-by-step learning path
                4. Practice problems progression (easy → hard)
                5. Time estimate to master this topic
                6. Common mistakes to avoid
                7. Interview questions to practice
                
                Be comprehensive but concise. Focus on practical application.
                """.formatted(topic, currentLevel, targetLevel);
    }

    public static String getWeakAreaAnalysisPrompt(String weakTopics, String userStats) {
        return """
                You are a coding interview coach analyzing a candidate's weak areas.
                
                WEAK TOPICS:
                %s
                
                USER STATISTICS:
                %s
                
                Analyze and provide:
                1. Why these topics are commonly weak for candidates
                2. Prerequisites or foundational topics to review
                3. Specific problem patterns in each weak topic
                4. Recommended resources (books, websites, courses)
                5. A prioritized improvement plan
                6. Daily practice recommendations
                
                Be encouraging but realistic about the effort required.
                """.formatted(weakTopics, userStats);
    }

    public static String getCompanyPreparationPrompt(String company, String userStrengths, String userWeaknesses) {
        return """
                You are an expert in %s technical interviews. Tailor interview preparation for this company.
                
                USER STRENGTHS:
                %s
                
                USER WEAKNESSES:
                %s
                
                Provide company-specific preparation advice:
                1. Most common question types at %s
                2. Focus areas based on their tech stack
                3. Interview format and what to expect
                4. Time management strategies
                5. How to leverage user's strengths
                6. How to address weaknesses
                7. Example questions typical for %s
                8. Final tips and preparation timeline
                
                Be specific to %s's interview process and culture.
                """.formatted(company, userStrengths, userWeaknesses, company, company, company);
    }

    public String getMockInterviewFeedback(String problemStatement, String userSolution, String optimalSolution) {
        return """
                You are an expert coding interview evaluator. Provide detailed feedback on this interview attempt.
                
                PROBLEM:
                %s
                
                USER'S SOLUTION:
                %s
                
                OPTIMAL SOLUTION:
                %s
                
                Provide feedback on:
                1. Time Complexity Analysis (user vs optimal)
                2. Space Complexity Analysis (user vs optimal)
                3. Code Quality and Best Practices
                4. Problem-Solving Approach
                5. Communication (if applicable)
                6. Areas of Improvement
                7. Positive Aspects
                8. Next Steps for Practice
                
                Be constructive and encouraging. Highlight both strengths and areas for growth.
                """.formatted(problemStatement, userSolution, optimalSolution);
    }

    public String getDailyLearningTipPrompt(String topic, String difficulty) {
        return """
                You are a coding tutor. Generate a brief, helpful daily learning tip for a candidate.
                
                TOPIC: %s
                DIFFICULTY LEVEL: %s
                
                Provide:
                1. A concise tip or concept explanation
                2. A practical example or pattern
                3. One practice problem suggestion
                4. Time to complete: X minutes
                
                Keep it focused and actionable for daily practice.
                """.formatted(topic, difficulty);
    }

    public String getContestStrategyPrompt(String contestType, String userLevel) {
        return """
                You are an expert competitive programmer. Provide strategies for success in %s contests.
                
                USER LEVEL: %s
                
                Provide:
                1. Problem-solving approach (read, analyze, code, test)
                2. Time allocation strategy
                3. Common pitfalls to avoid
                4. Tools and techniques for this contest type
                5. Practice resources
                6. Mental preparation tips
                
                Be practical and specific to %s contests.
                """.formatted(contestType, userLevel, contestType);
    }
}
