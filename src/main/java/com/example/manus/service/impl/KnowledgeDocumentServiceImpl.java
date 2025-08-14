package com.example.manus.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.manus.persistence.entity.KnowledgeDocument;
import com.example.manus.persistence.mapper.KnowledgeDocumentMapper;
import com.example.manus.service.KnowledgeBaseService;
import com.example.manus.service.KnowledgeDocumentService;
import com.example.manus.service.VectorProcessingService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class KnowledgeDocumentServiceImpl implements KnowledgeDocumentService {

    private final KnowledgeDocumentMapper knowledgeDocumentMapper;
    private final KnowledgeBaseService knowledgeBaseService;
    private final VectorProcessingService vectorProcessingService;

    @Value("${app.file.upload-dir:./uploads}")
    private String uploadDir;

    @Override
    public KnowledgeDocument uploadDocument(String knowledgeBaseId, MultipartFile file) {
        // 检查知识库访问权限
        if (!knowledgeBaseService.hasAccessPermission(knowledgeBaseId, StpUtil.getLoginIdAsString())) {
            throw new RuntimeException("无权限访问此知识库");
        }

        if (file.isEmpty()) {
            throw new RuntimeException("文件不能为空");
        }

        try {
            // 创建上传目录
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // 生成文件名
            String documentId = UUID.randomUUID().toString();
            String fileName = documentId + "_" + file.getOriginalFilename();
            Path filePath = uploadPath.resolve(fileName);

            // 保存文件
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // 创建文档记录
            KnowledgeDocument document = new KnowledgeDocument();
            document.setId(documentId);
            document.setKnowledgeBaseId(knowledgeBaseId);
            document.setName(file.getOriginalFilename());
            document.setOriginalName(file.getOriginalFilename());
            document.setFileType(getFileExtension(file.getOriginalFilename()));
            document.setFileSize(file.getSize());
            document.setFilePath(filePath.toString());
            document.setProcessStatus(0); // 待处理
            document.setUserId(StpUtil.getLoginIdAsString());
            document.setCreatedAt(LocalDateTime.now());
            document.setUpdatedAt(LocalDateTime.now());

            knowledgeDocumentMapper.insert(document);

            // 异步处理文档
            processDocument(documentId);

            return document;

        } catch (IOException e) {
            throw new RuntimeException("文件保存失败: " + e.getMessage());
        }
    }

    @Override
    public void deleteDocument(String documentId) {
        KnowledgeDocument document = knowledgeDocumentMapper.selectById(documentId);
        if (document == null) {
            throw new RuntimeException("文档不存在");
        }

        // 检查权限
        if (!hasAccessPermission(documentId, StpUtil.getLoginIdAsString())) {
            throw new RuntimeException("无权限删除此文档");
        }

        // 删除物理文件
        try {
            Path filePath = Paths.get(document.getFilePath());
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            // 记录日志但不阻塞删除操作
            System.err.println("删除物理文件失败: " + e.getMessage());
        }

        // 删除数据库记录
        knowledgeDocumentMapper.deleteById(documentId);

        // 更新知识库统计信息
        knowledgeBaseService.updateKnowledgeBaseStats(document.getKnowledgeBaseId());
    }

    @Override
    public KnowledgeDocument getDocumentById(String documentId) {
        KnowledgeDocument document = knowledgeDocumentMapper.selectById(documentId);
        if (document == null) {
            return null;
        }

        // 检查权限
        if (!hasAccessPermission(documentId, StpUtil.getLoginIdAsString())) {
            throw new RuntimeException("无权限访问此文档");
        }

        return document;
    }

    @Override
    public List<KnowledgeDocument> getDocumentsByKnowledgeBase(String knowledgeBaseId) {
        // 检查知识库访问权限
        if (!knowledgeBaseService.hasAccessPermission(knowledgeBaseId, StpUtil.getLoginIdAsString())) {
            throw new RuntimeException("无权限访问此知识库");
        }

        return knowledgeDocumentMapper.selectByKnowledgeBaseId(knowledgeBaseId);
    }

    @Override
    public Page<KnowledgeDocument> searchDocuments(long current, long size, String knowledgeBaseId, String keyword) {
        // 检查知识库访问权限
        if (StringUtils.hasText(knowledgeBaseId)) {
            if (!knowledgeBaseService.hasAccessPermission(knowledgeBaseId, StpUtil.getLoginIdAsString())) {
                throw new RuntimeException("无权限访问此知识库");
            }
        }

        Page<KnowledgeDocument> page = new Page<>(current, size);
        LambdaQueryWrapper<KnowledgeDocument> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(knowledgeBaseId)) {
            wrapper.eq(KnowledgeDocument::getKnowledgeBaseId, knowledgeBaseId);
        }

        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(KnowledgeDocument::getName, keyword)
                    .or().like(KnowledgeDocument::getOriginalName, keyword));
        }

        wrapper.orderByDesc(KnowledgeDocument::getCreatedAt);
        return knowledgeDocumentMapper.selectPage(page, wrapper);
    }

    @Override
    public void processDocument(String documentId) {
        // 更新处理状态为处理中
        updateProcessStatus(documentId, 1, null);

        try {
            // 调用向量处理服务
            int chunkCount = vectorProcessingService.processDocument(documentId);

            // 更新处理状态为已完成
            updateProcessStatus(documentId, 2, chunkCount);

        } catch (Exception e) {
            // 更新处理状态为失败
            updateProcessStatus(documentId, 3, null);
            throw new RuntimeException("文档处理失败: " + e.getMessage());
        }
    }

    @Override
    public void updateProcessStatus(String documentId, Integer status, Integer chunkCount) {
        knowledgeDocumentMapper.updateProcessStatus(documentId, status, chunkCount);
    }

    @Override
    public boolean hasAccessPermission(String documentId, String userId) {
        KnowledgeDocument document = knowledgeDocumentMapper.selectById(documentId);
        if (document == null) {
            return false;
        }

        // 检查知识库访问权限
        return knowledgeBaseService.hasAccessPermission(document.getKnowledgeBaseId(), userId);
    }

    private String getFileExtension(String filename) {
        if (filename == null || filename.isEmpty()) {
            return "";
        }
        int lastDotIndex = filename.lastIndexOf(".");
        if (lastDotIndex == -1) {
            return "";
        }
        return filename.substring(lastDotIndex + 1).toLowerCase();
    }
}