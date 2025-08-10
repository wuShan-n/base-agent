package com.example.manus.controller;

import com.example.manus.common.CommonResult;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final EmbeddingStoreIngestor embeddingStoreIngestor;

    /**
     * 处理文件上传请求，将文档内容加载、切分、向量化并存入向量数据库。
     * @param file 用户上传的文件 (e.g., PDF, TXT, DOCX).
     * @return CommonResult<String> 包含操作结果的通用响应体
     */
    @PostMapping("/upload")
    public CommonResult<String> handleFileUpload(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return CommonResult.validateFailed("上传失败，请选择一个文件。");
        }

        try {
            Path tempFile = Files.createTempFile("upload-", file.getOriginalFilename());
            Files.copy(file.getInputStream(), tempFile, StandardCopyOption.REPLACE_EXISTING);
            File localFile = tempFile.toFile();

            localFile.deleteOnExit();

            Document document = FileSystemDocumentLoader.loadDocument(localFile.toPath());

            embeddingStoreIngestor.ingest(document);

            String successMessage = "文件 '" + file.getOriginalFilename() + "' 已成功处理并添加到知识库。";
            return CommonResult.success(successMessage);

        } catch (IOException e) {
            // 异常处理
            return CommonResult.failed("处理文件时发生错误: " + e.getMessage());
        }
    }
}
