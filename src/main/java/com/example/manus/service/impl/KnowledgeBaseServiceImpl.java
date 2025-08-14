package com.example.manus.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.manus.persistence.entity.KnowledgeBase;
import com.example.manus.persistence.mapper.KnowledgeBaseMapper;
import com.example.manus.persistence.mapper.KnowledgeDocumentMapper;
import com.example.manus.service.KnowledgeBaseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class KnowledgeBaseServiceImpl implements KnowledgeBaseService {

    private final KnowledgeBaseMapper knowledgeBaseMapper;
    private final KnowledgeDocumentMapper knowledgeDocumentMapper;

    @Override
    public KnowledgeBase createKnowledgeBase(KnowledgeBase knowledgeBase) {
        knowledgeBase.setId(UUID.randomUUID().toString());
        knowledgeBase.setUserId(StpUtil.getLoginIdAsString());
        knowledgeBase.setStatus(1);
        knowledgeBase.setDocumentCount(0);
        knowledgeBase.setTotalSize(0L);
        knowledgeBase.setCreatedAt(LocalDateTime.now());
        knowledgeBase.setUpdatedAt(LocalDateTime.now());
        knowledgeBase.setLastUpdated(LocalDateTime.now());

        knowledgeBaseMapper.insert(knowledgeBase);
        return knowledgeBase;
    }

    @Override
    public KnowledgeBase updateKnowledgeBase(KnowledgeBase knowledgeBase) {
        KnowledgeBase existing = knowledgeBaseMapper.selectById(knowledgeBase.getId());
        if (existing == null) {
            throw new RuntimeException("知识库不存在");
        }

        // 检查权限
        if (!hasAccessPermission(knowledgeBase.getId(), StpUtil.getLoginIdAsString())) {
            throw new RuntimeException("无权限操作此知识库");
        }

        knowledgeBase.setUpdatedAt(LocalDateTime.now());
        knowledgeBaseMapper.updateById(knowledgeBase);
        return knowledgeBaseMapper.selectById(knowledgeBase.getId());
    }

    @Override
    public void deleteKnowledgeBase(String id) {
        KnowledgeBase knowledgeBase = knowledgeBaseMapper.selectById(id);
        if (knowledgeBase == null) {
            throw new RuntimeException("知识库不存在");
        }

        // 检查权限
        if (!hasAccessPermission(id, StpUtil.getLoginIdAsString())) {
            throw new RuntimeException("无权限删除此知识库");
        }

        // 软删除
        knowledgeBase.setStatus(0);
        knowledgeBase.setUpdatedAt(LocalDateTime.now());
        knowledgeBaseMapper.updateById(knowledgeBase);
    }

    @Override
    public KnowledgeBase getKnowledgeBaseById(String id) {
        KnowledgeBase knowledgeBase = knowledgeBaseMapper.selectById(id);
        if (knowledgeBase == null || knowledgeBase.getStatus() == 0) {
            return null;
        }

        // 检查访问权限
        if (!hasAccessPermission(id, StpUtil.getLoginIdAsString())) {
            throw new RuntimeException("无权限访问此知识库");
        }

        return knowledgeBase;
    }

    @Override
    public List<KnowledgeBase> getUserKnowledgeBases(String userId) {
        return knowledgeBaseMapper.selectByUserId(userId);
    }

    @Override
    public List<KnowledgeBase> getPublicKnowledgeBases() {
        return knowledgeBaseMapper.selectPublicKnowledgeBases();
    }

    @Override
    public Page<KnowledgeBase> searchKnowledgeBases(long current, long size, String keyword, String userId, Integer isPublic) {
        Page<KnowledgeBase> page = new Page<>(current, size);
        LambdaQueryWrapper<KnowledgeBase> wrapper = new LambdaQueryWrapper<>();

        wrapper.eq(KnowledgeBase::getStatus, 1);

        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(KnowledgeBase::getName, keyword)
                    .or().like(KnowledgeBase::getDescription, keyword));
        }

        if (StringUtils.hasText(userId)) {
            wrapper.eq(KnowledgeBase::getUserId, userId);
        }

        if (isPublic != null) {
            wrapper.eq(KnowledgeBase::getIsPublic, isPublic);
        }

        wrapper.orderByDesc(KnowledgeBase::getCreatedAt);
        return knowledgeBaseMapper.selectPage(page, wrapper);
    }

    @Override
    public boolean hasAccessPermission(String knowledgeBaseId, String userId) {
        KnowledgeBase knowledgeBase = knowledgeBaseMapper.selectById(knowledgeBaseId);
        if (knowledgeBase == null || knowledgeBase.getStatus() == 0) {
            return false;
        }

        // 拥有者有完全访问权限
        if (knowledgeBase.getUserId().equals(userId)) {
            return true;
        }

        // 公开的知识库任何人都可以访问
        return knowledgeBase.getIsPublic() == 1;
    }

    @Override
    public void updateKnowledgeBaseStats(String knowledgeBaseId) {
        int documentCount = knowledgeDocumentMapper.countByKnowledgeBaseId(knowledgeBaseId);
        long totalSize = knowledgeDocumentMapper.sumFileSizeByKnowledgeBaseId(knowledgeBaseId);

        KnowledgeBase knowledgeBase = new KnowledgeBase();
        knowledgeBase.setId(knowledgeBaseId);
        knowledgeBase.setDocumentCount(documentCount);
        knowledgeBase.setTotalSize(totalSize);
        knowledgeBase.setLastUpdated(LocalDateTime.now());
        knowledgeBase.setUpdatedAt(LocalDateTime.now());

        knowledgeBaseMapper.updateById(knowledgeBase);
    }
}