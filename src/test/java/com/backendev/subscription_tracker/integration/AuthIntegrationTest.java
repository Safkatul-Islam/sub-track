package com.backendev.subscription_tracker.integration;

import com.backendev.subscription_tracker.dto.RegistrationRequestDto;
import com.backendev.subscription_tracker.entity.Role;
import com.backendev.subscription_tracker.entity.Subscription;
import com.backendev.subscription_tracker.repository.SubscriptionRepository;
import com.backendev.subscription_tracker.repository.UserRepository;
import com.backendev.subscription_tracker.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AuthIntegrationTest {

    private static final String SUBSCRIPTIONS_URL = "/api/v1/subscriptions";
    private static final String REGISTER_URL = "/api/v1/auth/register";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Autowired
    private AuthService authService;

    private Long netflixId;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        subscriptionRepository.deleteAll();

        authService.registerWithRole(
                new RegistrationRequestDto("alice", "password123"), Role.USER);
        authService.registerWithRole(
                new RegistrationRequestDto("bob", "adminpass123"), Role.ADMIN);

        Subscription netflix = new Subscription(
                null, "Netflix", new BigDecimal("15.99"),
                LocalDate.now().plusDays(10), null);
        netflixId = subscriptionRepository.save(netflix).getId();
    }

    // ---------- Registration endpoint ----------

    @Test
    @DisplayName("POST /auth/register should create a USER and return 201")
    void register_withValidPayload_shouldReturn201() throws Exception {
        String payload = """
                {"username": "charlie", "password": "password123"}
                """;

        mockMvc.perform(post(REGISTER_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.username").value("charlie"))
                .andExpect(jsonPath("$.role").value("USER"));
    }

    @Test
    @DisplayName("POST /auth/register with existing username should return 409")
    void register_withDuplicateUsername_shouldReturn409() throws Exception {
        String payload = """
                {"username": "alice", "password": "password123"}
                """;

        mockMvc.perform(post(REGISTER_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("User Already Exists"));
    }

    @Test
    @DisplayName("POST /auth/register with short password should return 400 validation error")
    void register_withShortPassword_shouldReturn400() throws Exception {
        String payload = """
                {"username": "dan", "password": "short"}
                """;

        mockMvc.perform(post(REGISTER_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.password").exists());
    }

    // ---------- Real HTTP Basic auth against a protected endpoint ----------

    @Test
    @DisplayName("GET /subscriptions with correct Basic credentials should return 200")
    void protectedEndpoint_withValidBasicAuth_shouldReturn200() throws Exception {
        mockMvc.perform(get(SUBSCRIPTIONS_URL).with(httpBasic("alice", "password123")))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /subscriptions with wrong password should return 401")
    void protectedEndpoint_withWrongPassword_shouldReturn401() throws Exception {
        mockMvc.perform(get(SUBSCRIPTIONS_URL).with(httpBasic("alice", "wrong-password")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /subscriptions with unknown user should return 401")
    void protectedEndpoint_withUnknownUser_shouldReturn401() throws Exception {
        mockMvc.perform(get(SUBSCRIPTIONS_URL).with(httpBasic("ghost", "password123")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /subscriptions with no credentials should return 401")
    void protectedEndpoint_withoutCredentials_shouldReturn401() throws Exception {
        mockMvc.perform(get(SUBSCRIPTIONS_URL))
                .andExpect(status().isUnauthorized());
    }

    // ---------- Role-based authorization on DELETE ----------

    @Test
    @DisplayName("DELETE as USER role should return 403 Forbidden")
    void delete_asUserRole_shouldReturn403() throws Exception {
        mockMvc.perform(delete(SUBSCRIPTIONS_URL + "/" + netflixId)
                        .with(httpBasic("alice", "password123")))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("DELETE as ADMIN role should return 204")
    void delete_asAdminRole_shouldReturn204() throws Exception {
        mockMvc.perform(delete(SUBSCRIPTIONS_URL + "/" + netflixId)
                        .with(httpBasic("bob", "adminpass123")))
                .andExpect(status().isNoContent());
    }
}