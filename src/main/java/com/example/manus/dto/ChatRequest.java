package com.example.manus.dto;

import lombok.Data;

import java.util.List;

/**
 *
 * Created by wuShan on 2025/8/14
 */
@Data
public class ChatRequest {
    private String conversationId;
    private String message;
    private List<String> tools; // 工具代码列表，如果为空则使用用户默认配置
    private boolean useUserConfig; // 是否使用用户配置的工具
    
    // 多Agent支持
    private String agentId; // 指定要使用的Agent ID
    private String agentCode; // 或者使用Agent代码
    private String customPrompt; // 自定义系统提示词
}
