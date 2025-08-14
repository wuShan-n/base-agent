package com.example.manus.service;

import java.util.List;

public interface DynamicToolLoadingService {

    List<Object> loadUserEnabledTools(String userId);

    List<Object> loadToolsByIds(List<String> toolIds);

    List<Object> loadToolsByCodes(List<String> toolCodes);

    Object loadToolByCode(String toolCode);

    boolean isToolAvailable(String toolCode);

    List<String> getAvailableToolCodes();
}