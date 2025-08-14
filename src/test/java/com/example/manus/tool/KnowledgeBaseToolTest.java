package com.example.manus.tool;

import dev.langchain4j.rag.content.Content;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.query.Query;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * Created by wuShan on 2025/8/12
 */
@SpringBootTest
class KnowledgeBaseToolTest {
    @Resource
    private  ContentRetriever contentRetriever;

    @Test
    void searchKnowledgeBase() {
        List<Content> relevantContent = contentRetriever.retrieve(Query.from("郭世纪会什么？"));
        System.out.println(relevantContent);
    }
}
