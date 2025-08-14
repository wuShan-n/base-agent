package com.example.manus.service;

import com.example.manus.dto.LoginRequest;
import com.example.manus.dto.LoginResponse;
import com.example.manus.dto.RegisterRequest;
import com.example.manus.persistence.entity.User;
import com.example.manus.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class AuthServiceTest {

    @Autowired
    private AuthService authService;

    @Test
    public void testRegisterAndLogin() {
        // 测试注册
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUsername("testuser");
        registerRequest.setPassword("testpass");
        registerRequest.setEmail("test@example.com");
        registerRequest.setNickname("测试用户");

        User user = authService.register(registerRequest);
        assertNotNull(user.getId());
        assertEquals("testuser", user.getUsername());
        assertEquals("测试用户", user.getNickname());
        assertNull(user.getPassword()); // 注册返回时密码应该为空

        // 测试登录
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("testpass");

        LoginResponse loginResponse = authService.login(loginRequest);
        assertNotNull(loginResponse.getToken());
        assertEquals("testuser", loginResponse.getUsername());
        assertEquals("测试用户", loginResponse.getNickname());
    }

    @Test
    public void testLoginWithWrongPassword() {
        // 先注册用户
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUsername("testuser2");
        registerRequest.setPassword("correctpass");
        authService.register(registerRequest);

        // 使用错误密码登录
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser2");
        loginRequest.setPassword("wrongpass");

        assertThrows(RuntimeException.class, () -> {
            authService.login(loginRequest);
        });
    }
}