package com.codeinsight.backend.dto;

public class CodingProblemDTO {
    private Long id;
    private String title;
    private String slug;
    private String platform;
    private String difficulty;
    private String topic;
    private String description;
    private String url;
    private double similarity;

    public CodingProblemDTO() {}

    public CodingProblemDTO(Long id, String title, String slug, String platform, String difficulty, String topic, String description, String url, double similarity) {
        this.id = id;
        this.title = title;
        this.slug = slug;
        this.platform = platform;
        this.difficulty = difficulty;
        this.topic = topic;
        this.description = description;
        this.url = url;
        this.similarity = similarity;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }

    public String getPlatform() { return platform; }
    public void setPlatform(String platform) { this.platform = platform; }

    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }

    public String getTopic() { return topic; }
    public void setTopic(String topic) { this.topic = topic; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public double getSimilarity() { return similarity; }
    public void setSimilarity(double similarity) { this.similarity = similarity; }
}