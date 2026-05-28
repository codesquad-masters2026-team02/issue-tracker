package com.codesquad.issueTracker.issue;

import static com.codesquad.issueTracker.TestFixtures.label;
import static com.codesquad.issueTracker.TestFixtures.milestone;
import static com.codesquad.issueTracker.TestFixtures.user;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.codesquad.issueTracker.comment.CommentService;
import com.codesquad.issueTracker.common.exception.BusinessException;
import com.codesquad.issueTracker.common.exception.ErrorCode;
import com.codesquad.issueTracker.issue.dto.request.AssigneeUpdateRequest;
import com.codesquad.issueTracker.issue.dto.request.BulkIssueRequest;
import com.codesquad.issueTracker.issue.dto.request.IssueRequest;
import com.codesquad.issueTracker.issue.dto.request.IssueSearchCondition;
import com.codesquad.issueTracker.issue.dto.request.IssueTitleUpdateRequest;
import com.codesquad.issueTracker.issue.dto.request.UpdateIssueStatusRequest;
import com.codesquad.issueTracker.issue.dto.response.IssueDetailResponse;
import com.codesquad.issueTracker.issue.dto.response.IssueSearchResponse;
import com.codesquad.issueTracker.label.Label;
import com.codesquad.issueTracker.label.LabelRepository;
import com.codesquad.issueTracker.label.dto.LabelUpdateRequest;
import com.codesquad.issueTracker.milestone.Milestone;
import com.codesquad.issueTracker.milestone.MilestoneRepository;
import com.codesquad.issueTracker.milestone.dto.MilestoneSidebarUpdateRequest;
import com.codesquad.issueTracker.user.User;
import com.codesquad.issueTracker.user.UserRepository;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class IssueServiceTest {

    @Autowired
    IssueService issueService;

    @Autowired
    IssueRepository issueRepository;

    @Autowired
    LabelRepository labelRepository;

    @Autowired
    MilestoneRepository milestoneRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    CommentService commentService;

    @Test
    void createsIssueWithLabelsMilestoneAssigneesAndIssueBodyComment() {
        User author = userRepository.save(user("issue-author"));
        User assignee = userRepository.save(user("issue-assignee"));
        Label bug = labelRepository.save(label("bug"));
        Milestone sprint = milestoneRepository.save(milestone("Sprint"));

        IssueDetailResponse created = issueService.create(
                new IssueRequest(
                        "Cannot login",
                        "Login fails with valid credentials",
                        List.of(bug.getId()),
                        sprint.getId(),
                        List.of(assignee.getId()),
                        List.of()
                ),
                author.getId()
        );

        assertThat(created.issueNumber()).isNotNull();
        assertThat(created.title()).isEqualTo("Cannot login");
        assertThat(created.authorUsername()).isEqualTo("issue-author");
        assertThat(created.labels()).singleElement()
                .extracting("labelId")
                .isEqualTo(bug.getId());
        assertThat(created.milestone().id()).isEqualTo(sprint.getId());
        assertThat(created.assignees()).singleElement()
                .extracting("id")
                .isEqualTo(assignee.getId());
        assertThat(commentService.getCommentListForIssue(created.issueNumber()).comments())
                .singleElement()
                .satisfies(comment -> {
                    assertThat(comment.type()).isEqualTo("ISSUE_BODY");
                    assertThat(comment.content()).isEqualTo("Login fails with valid credentials");
                    assertThat(comment.username()).isEqualTo("issue-author");
                });
    }

    @Test
    void getsIssueDetailWithMilestoneCountsReflectingIssueStatuses() {
        User author = userRepository.save(user("detail-author"));
        Milestone sprint = milestoneRepository.save(milestone("Count Sprint"));
        IssueDetailResponse openIssue = issueService.create(
                issueRequest("open", sprint.getId()),
                author.getId()
        );
        IssueDetailResponse closedIssue = issueService.create(
                issueRequest("closed", sprint.getId()),
                author.getId()
        );
        issueService.updateStatus(closedIssue.issueNumber(), new UpdateIssueStatusRequest(IssueStatus.CLOSED));

        IssueDetailResponse detail = issueService.findIssueById(openIssue.issueNumber());

        assertThat(detail.milestone().openIssueCount()).isEqualTo(1);
        assertThat(detail.milestone().closedIssueCount()).isEqualTo(1);
    }

    @Test
    void getsIssuesByStatusWithGlobalOpenAndClosedCounts() {
        User author = userRepository.save(user("list-author"));
        issueService.create(issueRequest("open 1", null), author.getId());
        issueService.create(issueRequest("open 2", null), author.getId());
        IssueDetailResponse closed = issueService.create(
                issueRequest("closed", null),
                author.getId()
        );
        issueService.updateStatus(closed.issueNumber(), new UpdateIssueStatusRequest(IssueStatus.CLOSED));

        IssueSearchResponse response = issueService.getIssues(new IssueSearchCondition(IssueStatus.OPEN, null,null,null,null,0));

        assertThat(response.openIssueCount()).isEqualTo(2);
        assertThat(response.closedIssueCount()).isEqualTo(1);
        assertThat(response.issues()).hasSize(2);
        assertThat(response.issues())
                .extracting("title")
                .containsExactlyInAnyOrder("open 1", "open 2");
    }

    @Test
    void bulkStatusUpdateChangesAllRequestedIssues() {
        User author = userRepository.save(user("bulk-author"));
        IssueDetailResponse first = issueService.create(
                issueRequest("first", null),
                author.getId()
        );
        IssueDetailResponse second = issueService.create(
                issueRequest("second", null),
                author.getId()
        );

        issueService.bulkUpdateStatus(new BulkIssueRequest(List.of(first.issueNumber(), second.issueNumber()), IssueStatus.CLOSED));

        assertThat(issueService.getIssues(new IssueSearchCondition(IssueStatus.CLOSED, null, null, null,null,0)).issues())
                .extracting("issueNumber")
                .contains(first.issueNumber(), second.issueNumber());
    }

    @Test
    void updatesIssueTitle() {
        User author = userRepository.save(user("title-author"));
        IssueDetailResponse issue = issueService.create(
                issueRequest("old title", null),
                author.getId()
        );

        issueService.updateTitle(issue.issueNumber(), new IssueTitleUpdateRequest("new title"));

        assertThat(issueService.findIssueById(issue.issueNumber()).title()).isEqualTo("new title");
    }

    @Test
    void sidebarUpdatesAssignAndRemoveAssigneesLabelsAndMilestone() {
        User author = userRepository.save(user("sidebar-author"));
        User assignee = userRepository.save(user("sidebar-assignee"));
        Label backend = labelRepository.save(label("backend"));
        Milestone sprint = milestoneRepository.save(milestone("Sidebar Sprint"));
        IssueDetailResponse issue = issueService.create(
                issueRequest("sidebar", null),
                author.getId()
        );

        issueService.updateAssignees(issue.issueNumber(), true, new AssigneeUpdateRequest(List.of(assignee.getId())));
        issueService.updateLabel(issue.issueNumber(), true, new LabelUpdateRequest(List.of(backend.getId())));
        issueService.updateMilestone(issue.issueNumber(), true, new MilestoneSidebarUpdateRequest(sprint.getId()));

        IssueDetailResponse assigned = issueService.findIssueById(issue.issueNumber());
        assertThat(assigned.assignees()).singleElement().extracting("id").isEqualTo(assignee.getId());
        assertThat(assigned.labels()).singleElement().extracting("labelId").isEqualTo(backend.getId());
        assertThat(assigned.milestone().id()).isEqualTo(sprint.getId());

        issueService.updateAssignees(issue.issueNumber(), false, new AssigneeUpdateRequest(List.of(assignee.getId())));
        issueService.updateLabel(issue.issueNumber(), false, new LabelUpdateRequest(List.of(backend.getId())));
        issueService.updateMilestone(issue.issueNumber(), false, null);

        IssueDetailResponse removed = issueService.findIssueById(issue.issueNumber());
        assertThat(removed.assignees()).isEmpty();
        assertThat(removed.labels()).isEmpty();
        assertThat(removed.milestone()).isNull();
    }

    @Test
    void updateAssigneesRejectsUnknownUser() {
        User author = userRepository.save(user("unknown-user-author"));
        IssueDetailResponse issue = issueService.create(
                issueRequest("unknown user", null),
                author.getId()
        );

        assertThatThrownBy(() -> issueService.updateAssignees(
                issue.issueNumber(),
                true,
                new AssigneeUpdateRequest(List.of(999_999L))
        ))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.USER_NOT_FOUND);
    }

    private IssueRequest issueRequest(String title, Long milestoneId) {
        return new IssueRequest(title, "body", List.of(), milestoneId, List.of(), List.<UUID>of());
    }
}
