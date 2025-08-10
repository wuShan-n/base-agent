package com.example.manus.tool;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class WebScraperTool {

    @Tool("根据一个指定的URL网址，获取其网页的纯文本内容。这对于从网页上提取信息非常有用。")
    public String scrapeWebsiteContent(@P("要抓取内容的完整URL网址") String url) {
        System.out.println(" WebScraperTool is called with URL: " + url);
        try {
            // 连接到目标URL并获取HTML文档
            Document doc = Jsoup.connect(url)
                                .timeout(10000) // 设置10秒超时
                                .get();

            // 提取网页的标题和纯文本内容
            String title = doc.title();
            String textContent = doc.body().text();

            // 返回一个格式化的字符串
            return String.format("网页标题: %s\n\n网页内容摘要:\n%s", title, textContent);

        } catch (IOException e) {
            // 如果抓取失败，返回一个清晰的错误信息
            return "无法访问或处理该URL: " + e.getMessage();
        }
    }
}
