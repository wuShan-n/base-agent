package com.example.manus.config;

import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * @author 21279
 * @description
 * @since 2025/8/3
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "langchain4j.embedding-model.open-ai")
public class EmbeddingModelConfig {
    String baseUrl;
    String modelName;

    @Bean
    public EmbeddingModel embeddingModel() {
        return OpenAiEmbeddingModel
                .builder()
                .timeout(Duration.ofSeconds(60))
                .baseUrl(baseUrl)
                .modelName(modelName)
                .build();
    }

}
