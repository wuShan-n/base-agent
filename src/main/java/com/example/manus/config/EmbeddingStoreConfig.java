package com.example.manus.config;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.pgvector.PgVectorEmbeddingStore;
import jakarta.annotation.Resource;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author 21279
 * @description
 * @since 2025/8/3
 */
@Configuration
@ConfigurationProperties(prefix = "spring.datasource")
@Data
public class EmbeddingStoreConfig {
    String url;
    String username;
    String password;
    @Resource
    private EmbeddingModel embeddingModel;

    @Bean
    public EmbeddingStore<TextSegment> embeddingStore() {
        return PgVectorEmbeddingStore.builder()
                .host(extractHost(url))
                .port(extractPort(url))
                .database(extractDatabase(url))
                .user(username)
                .password(password)
                .table("document_embeddings")
                .dimension(embeddingModel.dimension())
                .build();
    }

    private String extractHost(String url) {
        String[] parts = url.split("//")[1].split("/")[0].split(":");
        return parts[0];
    }

    private int extractPort(String url) {
        String[] parts = url.split("//")[1].split("/")[0].split(":");
        return parts.length > 1 ? Integer.parseInt(parts[1]) : 5432;
    }

    private String extractDatabase(String url) {
        return url.split("/")[url.split("/").length - 1];
    }
}
