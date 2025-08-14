package com.example.manus.service;

import cn.dev33.satoken.stp.StpUtil;
import com.example.manus.persistence.entity.KnowledgeBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class KnowledgeBaseServiceTest {

    @Autowired
    private KnowledgeBaseService knowledgeBaseService;

    @BeforeEach
    void setUp() {
        // 模拟用户登录
        StpUtil.login("1"); // 使用默认管理员用户
    }

    @Test
    void testCreateKnowledgeBase() {
        KnowledgeBase knowledgeBase = new KnowledgeBase();
        knowledgeBase.setName("测试知识库");
        knowledgeBase.setDescription("这是一个测试知识库");
        knowledgeBase.setIsPublic(0);

        KnowledgeBase created = knowledgeBaseService.createKnowledgeBase(knowledgeBase);

        assertNotNull(created.getId());
        assertEquals("测试知识库", created.getName());
        assertEquals("这是一个测试知识库", created.getDescription());
        assertEquals("1", created.getUserId());
        assertEquals(0, created.getIsPublic());
        assertEquals(1, created.getStatus());
        assertEquals(0, created.getDocumentCount());
        assertEquals(0L, created.getTotalSize());
    }

    @Test
    void testGetUserKnowledgeBases() {
        // 创建几个知识库
        KnowledgeBase kb1 = new KnowledgeBase();
        kb1.setName("知识库1");
        kb1.setDescription("描述1");
        kb1.setIsPublic(0);
        knowledgeBaseService.createKnowledgeBase(kb1);

        KnowledgeBase kb2 = new KnowledgeBase();
        kb2.setName("知识库2");
        kb2.setDescription("描述2");
        kb2.setIsPublic(1);
        knowledgeBaseService.createKnowledgeBase(kb2);

        List<KnowledgeBase> userKnowledgeBases = knowledgeBaseService.getUserKnowledgeBases("1");

        assertNotNull(userKnowledgeBases);
        assertEquals(2, userKnowledgeBases.size());
    }

    @Test
    void testHasAccessPermission() {
        // 创建知识库
        KnowledgeBase knowledgeBase = new KnowledgeBase();
        knowledgeBase.setName("权限测试知识库");
        knowledgeBase.setIsPublic(0); // 私有

        KnowledgeBase created = knowledgeBaseService.createKnowledgeBase(knowledgeBase);

        // 拥有者应该有访问权限
        assertTrue(knowledgeBaseService.hasAccessPermission(created.getId(), "1"));

        // 其他用户不应该有访问权限
        assertFalse(knowledgeBaseService.hasAccessPermission(created.getId(), "2"));

        // 设置为公开
        created.setIsPublic(1);
        knowledgeBaseService.updateKnowledgeBase(created);

        // 现在其他用户也应该有访问权限
        assertTrue(knowledgeBaseService.hasAccessPermission(created.getId(), "2"));
    }

    @Test
    void testUpdateKnowledgeBase() {
        // 创建知识库
        KnowledgeBase knowledgeBase = new KnowledgeBase();
        knowledgeBase.setName("原始名称");
        knowledgeBase.setDescription("原始描述");
        knowledgeBase.setIsPublic(0);

        KnowledgeBase created = knowledgeBaseService.createKnowledgeBase(knowledgeBase);

        // 更新知识库
        created.setName("更新后的名称");
        created.setDescription("更新后的描述");
        created.setIsPublic(1);

        KnowledgeBase updated = knowledgeBaseService.updateKnowledgeBase(created);

        assertEquals("更新后的名称", updated.getName());
        assertEquals("更新后的描述", updated.getDescription());
        assertEquals(1, updated.getIsPublic());
    }

    @Test
    void testDeleteKnowledgeBase() {
        // 创建知识库
        KnowledgeBase knowledgeBase = new KnowledgeBase();
        knowledgeBase.setName("待删除知识库");
        knowledgeBase.setDescription("这个知识库将被删除");

        KnowledgeBase created = knowledgeBaseService.createKnowledgeBase(knowledgeBase);
        assertNotNull(created.getId());

        // 删除知识库
        knowledgeBaseService.deleteKnowledgeBase(created.getId());

        // 尝试获取已删除的知识库应该返回null
        KnowledgeBase deleted = knowledgeBaseService.getKnowledgeBaseById(created.getId());
        assertNull(deleted);
    }
}