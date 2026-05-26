package com.codesquad.issueTracker.comment;

import static com.codesquad.issueTracker.TestFixtures.issue;
import static com.codesquad.issueTracker.TestFixtures.user;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.codesquad.issueTracker.comment.dto.CommentResponse;
import com.codesquad.issueTracker.issue.Issue;
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
class CommentControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    CommentService commentService;

    @Autowired
    IssueRepository issueRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    JwtHelper jwtHelper;

    @Test
    void createsAndGetsComments() throws Exception {
        User author = userRepository.save(user("comment-api-author"));
        Issue issue = issueRepository.save(issue(author.getId(), "comment api issue", IssueStatus.OPEN, null));

        mockMvc.perform(post("/api/issues/{id}/comments", issue.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearer(author))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"content":"hello"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.type").value("DISCUSSION"))
                .andExpect(jsonPath("$.data.username").value("comment-api-author"));

        mockMvc.perform(get("/api/issues/{id}/comments", issue.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearer(author)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.issueNumber").value(issue.getId()))
                .andExpect(jsonPath("$.data.comment_count").value(1))
                .andExpect(jsonPath("$.data.comments", hasSize(1)));
    }

    @Test
    void deletingSomeoneElsesCommentReturnsErrorDto() throws Exception {
        User owner = userRepository.save(user("comment-api-owner"));
        User other = userRepository.save(user("comment-api-other"));
        Issue issue = issueRepository.save(issue(owner.getId(), "forbidden comment issue", IssueStatus.OPEN, null));
        CommentResponse comment = commentService.postComment(
                issue.getId(),
                owner.getId(),
                new com.codesquad.issueTracker.comment.dto.CommentRequest("owned"),
                CommentType.DISCUSSION
        );

        mockMvc.perform(delete("/api/comments/{id}", comment.id())
                        .header(HttpHeaders.AUTHORIZATION, bearer(other)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("UNAUTHORIZED_MODIFICATION"))
                .andExpect(jsonPath("$.error.message").value("작성자만 삭제할 수 있습니다"));
    }

    @Test
    void deletingOwnCommentReturnsSuccessWrapper() throws Exception {
        User owner = userRepository.save(user("comment-api-delete-owner"));
        Issue issue = issueRepository.save(issue(owner.getId(), "delete comment issue", IssueStatus.OPEN, null));
        CommentResponse comment = commentService.postComment(
                issue.getId(),
                owner.getId(),
                new com.codesquad.issueTracker.comment.dto.CommentRequest("delete"),
                CommentType.DISCUSSION
        );

        mockMvc.perform(delete("/api/comments/{id}", comment.id())
                        .header(HttpHeaders.AUTHORIZATION, bearer(owner)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    private String bearer(User user) {
        return "Bearer " + jwtHelper.createUserAccessToken(user.getId());
    }
}
