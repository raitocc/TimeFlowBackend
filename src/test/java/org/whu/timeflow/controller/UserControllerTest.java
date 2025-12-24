package org.whu.timeflow.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import org.whu.timeflow.dto.UserDTO;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest // 启动整个Spring容器，模拟真实环境
@AutoConfigureMockMvc // 自动配置 MockMvc，用于模拟发送 HTTP 请求
@Transactional // 每个测试方法结束后，自动回滚数据库，保证清理现场！
@DisplayName("用户模块集成测试")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc; // 模拟客户端

    private final ObjectMapper objectMapper = new ObjectMapper(); // 用于把对象转成 JSON 字符串

    // 预设一个测试账号
    private final String TEST_EMAIL = "test_student@whu.edu.cn";
    private final String TEST_PWD = "password123";
    private final String NEW_PWD = "newPassword456";

    @Test
    @DisplayName("测试：用户全生命周期流程 (注册->登录->改名->改密)")
    void testFullUserLifeCycle() throws Exception {

        // =================Step 1: 注册 (Register) =================
        UserDTO registerDto = new UserDTO();
        registerDto.setEmail(TEST_EMAIL);
        registerDto.setPassword(TEST_PWD);
        registerDto.setNickname("测试小白");

        mockMvc.perform(post("/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerDto)))
                .andDo(print()) // 在控制台打印请求详情
                .andExpect(status().isOk()) // 期望状态码 200
                .andExpect(jsonPath("$.code").value(200)) // 期望返回 code=200
                .andExpect(jsonPath("$.msg").value("注册成功"));

        // ================= Step 2: 登录 (Login) =================
        UserDTO loginDto = new UserDTO();
        loginDto.setEmail(TEST_EMAIL);
        loginDto.setPassword(TEST_PWD);

        MvcResult loginResult = mockMvc.perform(post("/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDto)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.token").exists()) // 期望 Token 存在
                .andReturn();

        // 提取 Token
        String responseStr = loginResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        Map responseMap = objectMapper.readValue(responseStr, Map.class);
        Map dataMap = (Map) responseMap.get("data");
        String token = (String) dataMap.get("token");
        String bearerToken = "Bearer " + token;

        System.out.println("👉 获取到的 Token: " + token);

        // ================= Step 3: 修改昵称 (鉴权测试) =================
        UserDTO nicknameDto = new UserDTO();
        nicknameDto.setNickname("武大吴彦祖");

        mockMvc.perform(post("/user/update/nickname")
                        .header("Authorization", bearerToken) // ⚠ 带上 Token
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(nicknameDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.msg").value("昵称修改成功"));

        // ================= Step 4: 修改密码 (逻辑校验) =================
        UserDTO pwdDto = new UserDTO();
        pwdDto.setOldPassword(TEST_PWD); // 正确的旧密码
        pwdDto.setNewPassword(NEW_PWD);

        mockMvc.perform(post("/user/update/password")
                        .header("Authorization", bearerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pwdDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.msg").value("密码修改成功"));

        // ================= Step 5: 验证旧密码失效 =================
        // 尝试用旧密码再次登录，应该失败
        mockMvc.perform(post("/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDto))) // loginDto里还是旧密码
                .andExpect(jsonPath("$.code").value(500)) // 期望失败
                .andExpect(jsonPath("$.msg").value("密码错误"));
    }

    @Test
    @DisplayName("测试：重复注册拦截")
    void testDuplicateRegister() throws Exception {
        UserDTO user = new UserDTO();
        user.setEmail("duplicate@whu.edu.cn");
        user.setPassword("123456");

        // 第一次注册：成功
        mockMvc.perform(post("/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(jsonPath("$.code").value(200));

        // 第二次注册：失败
        mockMvc.perform(post("/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.msg").value("该邮箱已被注册"));
    }
}