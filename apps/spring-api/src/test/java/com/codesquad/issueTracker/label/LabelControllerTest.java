package com.codesquad.issueTracker.label;

import static com.codesquad.issueTracker.TestFixtures.user;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.codesquad.issueTracker.security.JwtHelper;
import com.codesquad.issueTracker.user.User;
import com.codesquad.issueTracker.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class LabelControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    LabelRepository labelRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    JwtHelper jwtHelper;

    @Test
    void createsListsUpdatesAndDeletesLabel() throws Exception {
        User user = userRepository.save(user("label-api-user"));
        String bearer = bearer(user);

        mockMvc.perform(post("/api/labels")
                        .header(HttpHeaders.AUTHORIZATION, bearer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"bug","description":"Bug","backgroundColor":"#FF0000","textColor":"LIGHT"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string(HttpHeaders.LOCATION, containsString("/api/labels/")))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("bug"));

        Long labelId = labelRepository.findAll().get(0).getId();

        mockMvc.perform(get("/api/labels").header(HttpHeaders.AUTHORIZATION, bearer))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.labels", hasSize(1)));

        mockMvc.perform(put("/api/labels/{id}", labelId)
                        .header(HttpHeaders.AUTHORIZATION, bearer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"backend","description":"Backend","backgroundColor":"#00FF00","textColor":"DARK"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("backend"))
                .andExpect(jsonPath("$.data.textColor").value("DARK"));

        mockMvc.perform(delete("/api/labels/{id}", labelId).header(HttpHeaders.AUTHORIZATION, bearer))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void missingLabelReturnsErrorDto() throws Exception {
        User user = userRepository.save(user("label-missing-api-user"));

        mockMvc.perform(get("/api/labels/{id}", 999_999L)
                        .header(HttpHeaders.AUTHORIZATION, bearer(user)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("LABEL_NOT_FOUND"));
    }

    private String bearer(User user) {
        return "Bearer " + jwtHelper.createUserAccessToken(user.getId());
    }
}
