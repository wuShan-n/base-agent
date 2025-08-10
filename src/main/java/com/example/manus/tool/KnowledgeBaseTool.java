package com.example.manus.tool;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.rag.content.Content;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.query.Query;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 一个封装了RAG检索能力的工具，允许Agent按需查询知识库。
 * 使用 @Component 注解，使其成为一个由Spring管理的Bean。
 */
@Component
@RequiredArgsConstructor
public class KnowledgeBaseTool {

    private final ContentRetriever contentRetriever;

    @Tool("当需要回答关于特定知识、文档或信息的问题时，使用此工具从知识库中检索相关内容。输入应该是需要查询的具体问题。")
    public String searchKnowledgeBase(@P("用户提出的需要从知识库中查找答案的问题") String query) {
        System.out.println(" RAG Tool is called with query: " + query);
        Query ragQuery = Query.from(query);
        List<Content> relevantContent = contentRetriever.retrieve(ragQuery);

        if (relevantContent.isEmpty()) {
            return "知识库中没有找到相关信息。";
        }

        return relevantContent.stream()
                .map(content -> content.textSegment().text())
                .collect(Collectors.joining("\n\n---\n\n"));
    }
}
