package com.backendev.subscription_tracker.integration;

import com.backendev.subscription_tracker.entity.Category;
import com.backendev.subscription_tracker.entity.Subscription;
import com.backendev.subscription_tracker.repository.SubscriptionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class SubscriptionIntegrationTest {

    private static final String BASE_URL = "/api/v1/subscriptions";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    private Long netflixId;

    @BeforeEach
    void seedDatabase() {
        subscriptionRepository.deleteAll();

        Subscription netflix = new Subscription(
                null, "Netflix", new BigDecimal("15.99"),
                LocalDate.now().plusDays(3), Category.STREAMING);
        Subscription spotify = new Subscription(
                null, "Spotify", new BigDecimal("9.99"),
                LocalDate.now().plusDays(20), Category.MUSIC);

        netflixId = subscriptionRepository.save(netflix).getId();
        subscriptionRepository.save(spotify);
    }

    private String jsonPayload(String name, String price, LocalDate renewalDate, String category) {
        return """
                {
                  "name": "%s",
                  "price": %s,
                  "renewalDate": "%s",
                  "category": "%s"
                }
                """.formatted(name, price, renewalDate, category);
    }

    // ---------- GET /api/v1/subscriptions ----------

    @Test
    @WithMockUser
    @DisplayName("GET should return 200 and a paginated list of seeded subscriptions")
    void getAllSubscriptions_shouldReturnSeededPage() throws Exception {
        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.content[0].id").exists())
                .andExpect(jsonPath("$.content[0].name").value("Netflix"))
                .andExpect(jsonPath("$.content[0].price").value(15.99))
                .andExpect(jsonPath("$.content[0].category").value("STREAMING"))
                .andExpect(jsonPath("$.content[1].name").value("Spotify"));
    }

    @Test
    @DisplayName("GET without authentication should return 401")
    void getAllSubscriptions_whenUnauthenticated_shouldReturn401() throws Exception {
        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isUnauthorized());
    }

    // ---------- GET /api/v1/subscriptions/upcoming ----------

    @Test
    @WithMockUser
    @DisplayName("GET /upcoming should return only subscriptions renewing within 7 days")
    void getUpcomingRenewals_shouldReturnOnlyWithinSevenDays() throws Exception {
        mockMvc.perform(get(BASE_URL + "/upcoming"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name").value("Netflix"));
    }

    // ---------- GET /api/v1/subscriptions/total-cost ----------

    @Test
    @WithMockUser
    @DisplayName("GET /total-cost should return the sum of all subscription prices")
    void getTotalMonthlyCost_shouldReturnSum() throws Exception {
        mockMvc.perform(get(BASE_URL + "/total-cost"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalMonthlyCost").value(25.98));
    }

    // ---------- POST /api/v1/subscriptions ----------

    @Test
    @WithMockUser
    @DisplayName("POST with valid payload should return 201 and the created resource")
    void addSubscription_withValidPayload_shouldReturn201() throws Exception {
        String payload = jsonPayload("Disney+", "19.99", LocalDate.now().plusDays(30), "STREAMING");

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Disney+"))
                .andExpect(jsonPath("$.price").value(19.99))
                .andExpect(jsonPath("$.category").value("STREAMING"));
    }

    @Test
    @WithMockUser
    @DisplayName("POST with duplicate name should return 409 Conflict (ProblemDetail)")
    void addSubscription_whenDuplicate_shouldReturn409() throws Exception {
        String payload = jsonPayload("Netflix", "19.99", LocalDate.now().plusDays(30), "STREAMING");

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Subscription Already Exists"))
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.detail").exists());
    }

    @Test
    @WithMockUser
    @DisplayName("POST with blank name should return 400 validation ProblemDetail")
    void addSubscription_withBlankName_shouldReturn400() throws Exception {
        String payload = jsonPayload("", "19.99", LocalDate.now().plusDays(30), "STREAMING");

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation Error"))
                .andExpect(jsonPath("$.errors.name").exists());
    }

    @Test
    @WithMockUser
    @DisplayName("POST with past renewal date should return 400 validation ProblemDetail")
    void addSubscription_withPastRenewalDate_shouldReturn400() throws Exception {
        String payload = jsonPayload("Hulu", "19.99", LocalDate.now().minusDays(1), "STREAMING");

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.renewalDate").exists());
    }

    // ---------- PUT /api/v1/subscriptions/{id} ----------

    @Test
    @WithMockUser
    @DisplayName("PUT with valid payload should return 200 and the updated resource")
    void updateSubscription_withValidPayload_shouldReturn200() throws Exception {
        String payload = jsonPayload("Netflix Premium", "24.99", LocalDate.now().plusDays(30), "STREAMING");

        mockMvc.perform(put(BASE_URL + "/" + netflixId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(netflixId))
                .andExpect(jsonPath("$.name").value("Netflix Premium"))
                .andExpect(jsonPath("$.price").value(24.99));
    }

    @Test
    @WithMockUser
    @DisplayName("PUT with non-existent ID should return 404 ProblemDetail")
    void updateSubscription_whenNotFound_shouldReturn404() throws Exception {
        String payload = jsonPayload("Ghost", "19.99", LocalDate.now().plusDays(30), "OTHER");

        mockMvc.perform(put(BASE_URL + "/99999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Subscription Not Found"))
                .andExpect(jsonPath("$.detail").value("No subscription with ID 99999 exists!"));
    }

    // ---------- DELETE /api/v1/subscriptions/{id} ----------

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("DELETE with existing ID as ADMIN should return 204")
    void deleteSubscription_whenExistsAsAdmin_shouldReturn204() throws Exception {
        mockMvc.perform(delete(BASE_URL + "/" + netflixId))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("DELETE with non-existent ID as ADMIN should return 404 ProblemDetail")
    void deleteSubscription_whenNotFoundAsAdmin_shouldReturn404() throws Exception {
        mockMvc.perform(delete(BASE_URL + "/99999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Subscription Not Found"));
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("DELETE as a non-admin USER should return 403 Forbidden")
    void deleteSubscription_whenNotAdmin_shouldReturn403() throws Exception {
        mockMvc.perform(delete(BASE_URL + "/" + netflixId))
                .andExpect(status().isForbidden());
    }
}