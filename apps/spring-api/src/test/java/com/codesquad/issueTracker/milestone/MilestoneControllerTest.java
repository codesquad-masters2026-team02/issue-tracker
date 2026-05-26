package com.codesquad.issueTracker.milestone;

import static com.codesquad.issueTracker.TestFixtures.issue;
import static com.codesquad.issueTracker.TestFixtures.milestone;
import static com.codesquad.issueTracker.TestFixtures.user;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.codesquad.issueTracker.issue.IssueRepository;
import com.codesquad.issueTracker.issue.IssueStatus;
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
class MilestoneControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    MilestoneRepository milestoneRepository;

    @Autowired
    IssueRepository issueRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    JwtHelper jwtHelper;

    @Test
    void createMilestoneReturnsCreatedWrappedResponse() throws Exception {
        User user = userRepository.save(user("milestone-api-user"));

        mockMvc.perform(post("/api/milestones")
                        .header(HttpHeaders.AUTHORIZATION, bearer(user))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"API Sprint","description":"created","dueDate":"2026. 06. 01"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string(HttpHeaders.LOCATION, containsString("/api/milestones")))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("API Sprint"))
                .andExpect(jsonPath("$.data.openIssueCount").value(0))
                .andExpect(jsonPath("$.data.closedIssueCount").value(0));
    }

    @Test
    void getMilestoneListFiltersByStatusAndIncludesIssueCounts() throws Exception {
        User author = userRepository.save(user("milestone-list-author"));
        Milestone open = milestoneRepository.save(milestone("Open API Sprint"));
        Milestone closed = milestoneRepository.save(milestone("Closed API Sprint"));
        closed.changeStatus(MilestoneStatus.CLOSED);
        milestoneRepository.save(closed);
        issueRepository.save(issue(author.getId(), "open issue", IssueStatus.OPEN, open.getId()));
        issueRepository.save(issue(author.getId(), "closed issue", IssueStatus.CLOSED, open.getId()));

        mockMvc.perform(get("/api/milestones")
                        .header(HttpHeaders.AUTHORIZATION, bearer(author))
                        .param("status", "OPEN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.openMilestoneCount").value(1))
                .andExpect(jsonPath("$.data.closedMilestoneCount").value(1))
                .andExpect(jsonPath("$.data.milestones", hasSize(1)))
                .andExpect(jsonPath("$.data.milestones[0].id").value(open.getId()))
                .andExpect(jsonPath("$.data.milestones[0].openIssueCount").value(1))
                .andExpect(jsonPath("$.data.milestones[0].closedIssueCount").value(1));
    }

    @Test
    void updatePatchAndDeleteMilestoneEndpointsWork() throws Exception {
        User user = userRepository.save(user("milestone-mutating-user"));
        Milestone milestone = milestoneRepository.save(milestone("Mutable Sprint"));

        mockMvc.perform(put("/api/milestones/{id}", milestone.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearer(user))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Updated Sprint","description":"updated","dueDate":"2026. 06. 02"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Updated Sprint"));

        mockMvc.perform(patch("/api/milestones/{id}", milestone.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearer(user))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"status":"CLOSED"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("CLOSED"));

        mockMvc.perform(delete("/api/milestones/{id}", milestone.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearer(user)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    private String bearer(User user) {
        return "Bearer " + jwtHelper.createUserAccessToken(user.getId());
    }
}
