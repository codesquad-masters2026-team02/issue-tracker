package com.codesquad.issueTracker.issue;

import static com.codesquad.issueTracker.TestFixtures.label;
import static com.codesquad.issueTracker.TestFixtures.milestone;
import static com.codesquad.issueTracker.TestFixtures.user;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.codesquad.issueTracker.issue.dto.request.IssueRequest;
import com.codesquad.issueTracker.issue.dto.response.IssueDetailResponse;
import com.codesquad.issueTracker.label.Label;
import com.codesquad.issueTracker.label.LabelRepository;
import com.codesquad.issueTracker.milestone.Milestone;
import com.codesquad.issueTracker.milestone.MilestoneRepository;
import com.codesquad.issueTracker.security.JwtHelper;
import com.codesquad.issueTracker.user.User;
import com.codesquad.issueTracker.user.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.UUID;
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
class IssueControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    IssueService issueService;

    @Autowired
    UserRepository userRepository;

    @Autowired
    LabelRepository labelRepository;

    @Autowired
    MilestoneRepository milestoneRepository;

    @Autowired
    JwtHelper jwtHelper;

    @Test
    void protectedEndpointWithoutTokenReturnsErrorDto() throws Exception {
        mockMvc.perform(get("/api/issues").param("status", "OPEN"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("LOGIN_REQUIRED"));
    }

    @Test
    void getIssuesAcceptsFlatStatusQueryParameter() throws Exception {
        User author = userRepository.save(user("issue-api-author"));
        IssueDetailResponse issue = issueService.create(
                issueRequest("api issue"),
                author.getId()
        );

        mockMvc.perform(get("/api/issues")
                        .header(HttpHeaders.AUTHORIZATION, bearer(author))
                        .param("status", "OPEN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.openIssueCount").value(1))
                .andExpect(jsonPath("$.data.issues", hasSize(1)))
                .andExpect(jsonPath("$.data.issues[0].issueNumber").value(issue.issueNumber()));
    }

    @Test
    void createIssueReturnsCreatedLocationAndWrappedResponse() throws Exception {
        User author = userRepository.save(user("create-api-author"));
        User assignee = userRepository.save(user("create-api-assignee"));
        Label label = labelRepository.save(label("api-label"));
        Milestone milestone = milestoneRepository.save(milestone("API Sprint"));
        IssueRequest request = new IssueRequest(
                "created through api",
                "api body",
                List.of(label.getId()),
                milestone.getId(),
                List.of(assignee.getId()),
                List.of()
        );

        mockMvc.perform(post("/api/issues")
                        .header(HttpHeaders.AUTHORIZATION, bearer(author))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string(HttpHeaders.LOCATION, containsString("/api/issues/")))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").value("created through api"))
                .andExpect(jsonPath("$.data.labels[0].labelId").value(label.getId()))
                .andExpect(jsonPath("$.data.assignees[0].id").value(assignee.getId()));
    }

    @Test
    void issueStatusAndSidebarEndpointsReturnNoContentWrapper() throws Exception {
        User author = userRepository.save(user("sidebar-api-author"));
        User assignee = userRepository.save(user("sidebar-api-assignee"));
        Label label = labelRepository.save(label("sidebar-api-label"));
        Milestone milestone = milestoneRepository.save(milestone("Sidebar API Sprint"));
        IssueDetailResponse issue = issueService.create(
                issueRequest("sidebar api"),
                author.getId()
        );

        mockMvc.perform(patch("/api/issues/{id}", issue.issueNumber())
                        .header(HttpHeaders.AUTHORIZATION, bearer(author))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"status":"CLOSED"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        mockMvc.perform(patch("/api/issues/{id}/title", issue.issueNumber())
                        .header(HttpHeaders.AUTHORIZATION, bearer(author))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"updated sidebar api"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        mockMvc.perform(post("/api/issues/{id}/assignees", issue.issueNumber())
                        .header(HttpHeaders.AUTHORIZATION, bearer(author))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userIds\":[" + assignee.getId() + "]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        mockMvc.perform(post("/api/issues/{id}/labels", issue.issueNumber())
                        .header(HttpHeaders.AUTHORIZATION, bearer(author))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"labelIds\":[" + label.getId() + "]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        mockMvc.perform(post("/api/issues/{id}/milestones", issue.issueNumber())
                        .header(HttpHeaders.AUTHORIZATION, bearer(author))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"milestoneId\":" + milestone.getId() + "}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        IssueDetailResponse updated = issueService.findIssueById(issue.issueNumber());
        assertThat(updated.title()).isEqualTo("updated sidebar api");
    }

    private String bearer(User user) {
        return "Bearer " + jwtHelper.createUserAccessToken(user.getId());
    }

    private IssueRequest issueRequest(String title) {
        return new IssueRequest(title, "body", List.of(), null, List.of(), List.<UUID>of());
    }
}
