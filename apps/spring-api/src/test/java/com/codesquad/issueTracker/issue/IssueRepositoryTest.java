package com.codesquad.issueTracker.issue;

import static com.codesquad.issueTracker.TestFixtures.issue;
import static com.codesquad.issueTracker.TestFixtures.milestone;
import static com.codesquad.issueTracker.TestFixtures.user;
import static org.assertj.core.api.Assertions.assertThat;

import com.codesquad.issueTracker.milestone.Milestone;
import com.codesquad.issueTracker.milestone.MilestoneRepository;
import com.codesquad.issueTracker.milestone.dto.MilestoneIssueCountDTO;
import com.codesquad.issueTracker.user.User;
import com.codesquad.issueTracker.user.UserRepository;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class IssueRepositoryTest {

    @Autowired
    IssueRepository issueRepository;

    @Autowired
    MilestoneRepository milestoneRepository;

    @Autowired
    UserRepository userRepository;

    @Test
    void countsIssuesByMilestoneAndStatus() {
        User author = userRepository.save(user("repo-author"));
        Milestone milestone = milestoneRepository.save(milestone("Repo Sprint"));
        issueRepository.save(issue(author.getId(), "open", IssueStatus.OPEN, milestone.getId()));
        issueRepository.save(issue(author.getId(), "closed", IssueStatus.CLOSED, milestone.getId()));
        issueRepository.save(issue(author.getId(), "unassigned", IssueStatus.OPEN, null));

        assertThat(issueRepository.countOpenIssuesByMilestoneId(milestone.getId())).isEqualTo(1);
        assertThat(issueRepository.countClosedIssuesByMilestoneId(milestone.getId())).isEqualTo(1);
    }

    @Test
    void aggregateMilestoneIssueCountsProjectIntoDto() {
        User author = userRepository.save(user("repo-count-author"));
        Milestone first = milestoneRepository.save(milestone("First Sprint"));
        Milestone second = milestoneRepository.save(milestone("Second Sprint"));
        issueRepository.save(issue(author.getId(), "first open", IssueStatus.OPEN, first.getId()));
        issueRepository.save(issue(author.getId(), "first closed", IssueStatus.CLOSED, first.getId()));
        issueRepository.save(issue(author.getId(), "second open", IssueStatus.OPEN, second.getId()));

        List<MilestoneIssueCountDTO> counts = issueRepository.countAllMilestonesIssueCounts();

        assertThat(counts)
                .filteredOn(count -> count.milestoneId().equals(first.getId()))
                .singleElement()
                .satisfies(count -> {
                    assertThat(count.openIssueCount()).isEqualTo(1);
                    assertThat(count.closedIssueCount()).isEqualTo(1);
                });
        assertThat(counts)
                .filteredOn(count -> count.milestoneId().equals(second.getId()))
                .singleElement()
                .satisfies(count -> {
                    assertThat(count.openIssueCount()).isEqualTo(1);
                    assertThat(count.closedIssueCount()).isZero();
                });
    }

    @Test
    void updateMilestoneDeletionClearsMilestoneReferences() {
        User author = userRepository.save(user("repo-delete-author"));
        Milestone milestone = milestoneRepository.save(milestone("Delete Sprint"));
        Issue issue = issueRepository.save(issue(author.getId(), "assigned", IssueStatus.OPEN, milestone.getId()));

        long updatedRows = issueRepository.updateMilestoneDeletion(milestone.getId());

        assertThat(updatedRows).isEqualTo(1);
        assertThat(issueRepository.findById(issue.getId())).get()
                .extracting(Issue::getMilestoneId)
                .isNull();
    }
}
