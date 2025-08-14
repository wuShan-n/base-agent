package com.example.manus.config;

import com.example.manus.util.FinalRetriever;
import com.example.manus.util.HttpRankingModel;
import com.example.manus.util.MultiQueryRetriever;
import dev.langchain4j.data.document.splitter.DocumentByParagraphSplitter;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 *
 * Created by wuShan on 2025/8/10
 */
@Configuration
@RequiredArgsConstructor
public class RagConfig {

    private final EmbeddingModel embeddingModel;

    private final EmbeddingStore<TextSegment> embeddingStore;

    private final ChatModel chatModel;

    @Value("${langchain4j.reranker.http.url}")
    private String rerankerUrl;



    @Bean
    public EmbeddingStoreIngestor embeddingStoreIngestor() {
        return EmbeddingStoreIngestor.builder().documentSplitter(new DocumentByParagraphSplitter(2000, 200)).embeddingModel(embeddingModel).embeddingStore(embeddingStore).build();
    }

    @Bean
    public ContentRetriever basicContentRetriever() {
        return EmbeddingStoreContentRetriever.builder()
                .minScore(0.3)
                .embeddingStore(embeddingStore)
                .embeddingModel(embeddingModel)
                .build();
    }



    // --- 定义召回层 Bean ---
    @Bean("recaller")
    public ContentRetriever recaller(@Qualifier("basicContentRetriever") ContentRetriever basicRetriever) {
        return new MultiQueryRetriever(
                chatModel,
                3,
                basicRetriever

        );
    }

    // --- 定义精排层 Bean ---
    @Bean
    public HttpRankingModel reranker() {
        return HttpRankingModel.builder()
                .rerankUrl(rerankerUrl)
                .build();
    }

    /**
     * --- 最终的、整合了召回与精排的 ContentRetriever ---
     * 这个 Bean 现在是我们整个 RAG 流程的入口点。
     */
    @Bean
    @Primary
    public ContentRetriever contentRetriever(@Qualifier("recaller") ContentRetriever recaller,
                                             HttpRankingModel reranker) {
        return new FinalRetriever(
                recaller,
                reranker,
                5
        );
    }


}
