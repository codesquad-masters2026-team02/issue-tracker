package com.codesquad.issueTracker.milestone;

import static com.codesquad.issueTracker.TestFixtures.issue;
import static com.codesquad.issueTracker.TestFixtures.milestone;
import static com.codesquad.issueTracker.TestFixtures.user;
import static org.assertj.core.api.Assertions.assertThat;

import com.codesquad.issueTracker.issue.Issue;
import com.codesquad.issueTracker.issue.IssueRepository;
import com.codesquad.issueTracker.issue.IssueStatus;
import com.codesquad.issueTracker.milestone.dto.MilestoneListResponse;
import com.codesquad.issueTracker.milestone.dto.MilestoneRequest;
import com.codesquad.issueTracker.milestone.dto.MilestoneResponse;
import com.codesquad.issueTracker.milestone.dto.MilestoneStatusUpdateRequest;
import com.codesquad.issueTracker.user.User;
import com.codesquad.issueTracker.user.UserRepository;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class MilestoneServiceTest {

    @Autowired
    MilestoneService milestoneService;

    @Autowired
    MilestoneRepository milestoneRepository;

    @Autowired
    IssueRepository issueRepository;

    @Autowired
    UserRepository userRepository;

    @Test
    void createsMilestoneWithZeroIssueCounts() {
        MilestoneResponse response = milestoneService.postNewMilestone(
                new MilestoneRequest("Sprint 1", "first sprint", LocalDate.of(2026, 6, 1))
        );

        assertThat(response.id()).isNotNull();
        assertThat(response.status()).isEqualTo(MilestoneStatus.OPEN);
        assertThat(response.openIssueCount()).isZero();
        assertThat(response.closedIssueCount()).isZero();
    }

    @Test
    void listsMilestonesByStatusWithOpenAndClosedIssueCounts() {
        User author = userRepository.save(user("author-one"));
        Milestone sprint = milestoneRepository.save(milestone("Sprint"));
        Milestone empty = milestoneRepository.save(milestone("Empty"));
        issueRepository.save(issue(author.getId(), "open 1", IssueStatus.OPEN, sprint.getId()));
        issueRepository.save(issue(author.getId(), "open 2", IssueStatus.OPEN, sprint.getId()));
        issueRepository.save(issue(author.getId(), "closed 1", IssueStatus.CLOSED, sprint.getId()));

        MilestoneListResponse response = milestoneService.getAllMilestonesByStatus(MilestoneStatus.OPEN);

        assertThat(response.openMilestoneCount()).isEqualTo(2);
        assertThat(response.closedMilestoneCount()).isZero();
        assertThat(response.milestoneCount()).isEqualTo(2);
        assertThat(response.milestones())
                .filteredOn(milestone -> milestone.id().equals(sprint.getId()))
                .singleElement()
                .satisfies(milestone -> {
                    assertThat(milestone.openIssueCount()).isEqualTo(2);
                    assertThat(milestone.closedIssueCount()).isEqualTo(1);
                });
        assertThat(response.milestones())
                .filteredOn(milestone -> milestone.id().equals(empty.getId()))
                .singleElement()
                .satisfies(milestone -> {
                    assertThat(milestone.openIssueCount()).isZero();
                    assertThat(milestone.closedIssueCount()).isZero();
                });
    }

    @Test
    void statusUpdateMovesMilestoneBetweenStatusListsAndKeepsIssueCounts() {
        User author = userRepository.save(user("author-two"));
        Milestone sprint = milestoneRepository.save(milestone("Closing Sprint"));
        issueRepository.save(issue(author.getId(), "open", IssueStatus.OPEN, sprint.getId()));
        issueRepository.save(issue(author.getId(), "closed", IssueStatus.CLOSED, sprint.getId()));

        MilestoneResponse closed = milestoneService.changeMilestoneStatusById(
                sprint.getId(),
                new MilestoneStatusUpdateRequest(MilestoneStatus.CLOSED)
        );

        assertThat(closed.status()).isEqualTo(MilestoneStatus.CLOSED);
        assertThat(closed.openIssueCount()).isEqualTo(1);
        assertThat(closed.closedIssueCount()).isEqualTo(1);
        assertThat(milestoneService.getAllMilestonesByStatus(MilestoneStatus.OPEN).milestones())
                .noneMatch(milestone -> milestone.id().equals(sprint.getId()));
        assertThat(milestoneService.getAllMilestonesByStatus(MilestoneStatus.CLOSED).milestones())
                .anyMatch(milestone -> milestone.id().equals(sprint.getId()));
    }

    @Test
    void deletingMilestoneSoftDeletesAndClearsIssueMilestoneReference() {
        User author = userRepository.save(user("author-three"));
        Milestone sprint = milestoneRepository.save(milestone("Deleted Sprint"));
        Issue issue = issueRepository.save(issue(author.getId(), "assigned issue", IssueStatus.OPEN, sprint.getId()));

        milestoneService.deleteMilestoneById(sprint.getId());

        assertThat(milestoneRepository.findActiveMilestoneById(sprint.getId())).isEmpty();
        assertThat(issueRepository.findById(issue.getId())).get()
                .extracting(Issue::getMilestoneId)
                .isNull();
    }
}
