package com.example.manus.util;

import dev.langchain4j.data.segment.TextSegment;
import lombok.Builder;
import lombok.Data;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 通过HTTP调用外部 Re-ranking 服务的客户端。
 * 这个类不再实现任何 LangChain4j 接口，是一个独立的组件。
 */
public class HttpRankingModel {

    private final String rerankUrl;
    private final RestTemplate restTemplate;

    @Builder
    public HttpRankingModel(String rerankUrl) {
        this.rerankUrl = rerankUrl;
        this.restTemplate = new RestTemplate();
    }

    @Data
    private static class RerankRequest {
        private String query;
        private List<String> documents;
    }

    @Data
    private static class RerankResult {
        private String document;
        private int index;
        private double relevance_score;
    }

    /**
     * 对给定的文档列表进行重排。
     * @param query    用户的原始查询。
     * @param segments 未排序的候选文档段落。
     * @return 根据相关性分数重新排序后的文档段落列表。
     */
    public List<TextSegment> rerank(String query, List<TextSegment> segments) {
        List<String> documentTexts = segments.stream()
                .map(TextSegment::text)
                .collect(Collectors.toList());

        RerankRequest requestPayload = new RerankRequest();
        requestPayload.setQuery(query);
        requestPayload.setDocuments(documentTexts);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<RerankRequest> entity = new HttpEntity<>(requestPayload, headers);

        try {
            ResponseEntity<List<RerankResult>> response = restTemplate.exchange(
                    rerankUrl,
                    HttpMethod.POST,
                    entity,
                    new ParameterizedTypeReference<>() {}
            );

            List<RerankResult> sortedResults = response.getBody();

            if (sortedResults == null) {
                return segments; // 如果API返回空，则返回原始顺序
            }

            // 根据API返回的排序，重新排列原始的 TextSegment 对象
            return sortedResults.stream()
                    .map(result -> segments.get(result.getIndex()))
                    .collect(Collectors.toList());

        } catch (Exception e) {
            System.err.println("调用 Re-ranker API 失败: " + e.getMessage());
            // 如果调用失败，安全起见返回原始顺序的文档
            return segments;
        }
    }
}
