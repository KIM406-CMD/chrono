package com.ncist.e2e;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("AuthController E2E 测试")
class AuthControllerE2ETest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("受保护接口未携带 Token 时返回 401 业务码")
    void protectedEndpointWithoutToken() throws Exception {
        mockMvc.perform(get("/api/user/info"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    @DisplayName("分类树为公开接口，无需登录即可访问")
    void categoryTreeIsPublic() throws Exception {
        mockMvc.perform(get("/api/category/tree"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }
}