package com.example.manus.tool;

import cn.dev33.satoken.stp.StpUtil;
import com.example.manus.service.KnowledgeSearchService;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.rag.content.Content;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class PersonalKnowledgeBaseTool {

    private final KnowledgeSearchService knowledgeSearchService;

    @Tool("当需要从用户的个人知识库中查找信息时使用此工具。此工具会搜索当前用户拥有的所有知识库。")
    public String searchMyKnowledgeBase(@P("需要从用户个人知识库中查找答案的问题") String query) {
        try {
            String userId = StpUtil.getLoginIdAsString();
            System.out.println("Personal Knowledge Base Tool called with query: " + query + " for user: " + userId);
            
            List<Content> relevantContent = knowledgeSearchService.searchByUser(userId, query, 5);

            if (relevantContent.isEmpty()) {
                return "您的个人知识库中没有找到相关信息。";
            }

            return relevantContent.stream()
                    .map(content -> content.textSegment().text())
                    .collect(Collectors.joining("\n\n---\n\n"));
                    
        } catch (Exception e) {
            return "搜索个人知识库时出现错误：" + e.getMessage();
        }
    }

    @Tool("当需要从指定知识库中查找信息时使用此工具。需要提供知识库ID。")
    public String searchSpecificKnowledgeBase(
            @P("知识库ID") String knowledgeBaseId,
            @P("需要从指定知识库中查找答案的问题") String query) {
        try {
            System.out.println("Specific Knowledge Base Tool called with query: " + query + " for KB: " + knowledgeBaseId);
            
            List<Content> relevantContent = knowledgeSearchService.searchByKnowledgeBase(knowledgeBaseId, query, 5);

            if (relevantContent.isEmpty()) {
                return "指定知识库中没有找到相关信息。";
            }

            return relevantContent.stream()
                    .map(content -> content.textSegment().text())
                    .collect(Collectors.joining("\n\n---\n\n"));
                    
        } catch (Exception e) {
            return "搜索指定知识库时出现错误：" + e.getMessage();
        }
    }

    @Tool("当需要从公开知识库中查找信息时使用此工具。")
    public String searchPublicKnowledgeBase(@P("需要从公开知识库中查找答案的问题") String query) {
        try {
            System.out.println("Public Knowledge Base Tool called with query: " + query);
            
            List<Content> relevantContent = knowledgeSearchService.searchPublic(query, 5);

            if (relevantContent.isEmpty()) {
                return "公开知识库中没有找到相关信息。";
            }

            return relevantContent.stream()
                    .map(content -> content.textSegment().text())
                    .collect(Collectors.joining("\n\n---\n\n"));
                    
        } catch (Exception e) {
            return "搜索公开知识库时出现错误：" + e.getMessage();
        }
    }
}