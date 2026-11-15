package org.mephi.learning;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mephi.learning.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = "app.security.enabled=true")
class AuthenticationIntegrationTest {
    @Autowired private MockMvc mvc;
    @Autowired private UserRepository users;

    @BeforeEach
    void cleanUsers() {
        users.deleteAll();
    }

    @Test
    void protectedEndpointRequiresAuthentication() throws Exception {
        mvc.perform(get("/api/users")).andExpect(status().isUnauthorized());
    }

    @Test
    void registrationAndLoginCreateSession() throws Exception {
        mvc.perform(
                        post("/api/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        "{\"name\":\"Sam\",\"email\":\"sam@example.test\",\"password\":\"secret123\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.role").value("STUDENT"));

        MvcResult login =
                mvc.perform(
                                post("/api/auth/login")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(
                                                "{\"email\":\"sam@example.test\",\"password\":\"secret123\"}"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.email").value("sam@example.test"))
                        .andReturn();

        MockHttpSession session = (MockHttpSession) login.getRequest().getSession(false);
        mvc.perform(get("/api/users").session(session)).andExpect(status().isOk());
    }

    @Test
    void studentCannotCreateCourse() throws Exception {
        mvc.perform(
                        post("/api/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        "{\"name\":\"Sam\",\"email\":\"sam@example.test\",\"password\":\"secret123\"}"))
                .andExpect(status().isCreated());
        MvcResult login =
                mvc.perform(
                                post("/api/auth/login")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(
                                                "{\"email\":\"sam@example.test\",\"password\":\"secret123\"}"))
                        .andExpect(status().isOk())
                        .andReturn();

        MockHttpSession session = (MockHttpSession) login.getRequest().getSession(false);
        mvc.perform(
                        post("/api/courses")
                                .session(session)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{}"))
                .andExpect(status().isForbidden());
    }
}
