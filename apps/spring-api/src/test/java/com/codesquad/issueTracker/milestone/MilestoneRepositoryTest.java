package com.codesquad.issueTracker.milestone;

import static com.codesquad.issueTracker.TestFixtures.milestone;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class MilestoneRepositoryTest {

    @Autowired
    MilestoneRepository milestoneRepository;

    @Test
    void findsOnlyActiveMilestonesByStatus() {
        Milestone open = milestoneRepository.save(milestone("Open Sprint"));
        Milestone closed = milestoneRepository.save(milestone("Closed Sprint"));
        closed.changeStatus(MilestoneStatus.CLOSED);
        milestoneRepository.save(closed);
        Milestone deleted = milestoneRepository.save(milestone("Deleted Sprint"));
        milestoneRepository.deleteMilestoneById(deleted.getId());

        assertThat(milestoneRepository.findAllByStatus(MilestoneStatus.OPEN))
                .extracting(Milestone::getId)
                .contains(open.getId())
                .doesNotContain(closed.getId(), deleted.getId());
        assertThat(milestoneRepository.findAllByStatus(MilestoneStatus.CLOSED))
                .extracting(Milestone::getId)
                .containsExactly(closed.getId());
    }

    @Test
    void countsOpenAndClosedActiveMilestones() {
        Milestone open = milestoneRepository.save(milestone("Open Count"));
        Milestone closed = milestoneRepository.save(milestone("Closed Count"));
        closed.changeStatus(MilestoneStatus.CLOSED);
        milestoneRepository.save(closed);
        Milestone deleted = milestoneRepository.save(milestone("Deleted Count"));
        milestoneRepository.deleteMilestoneById(deleted.getId());

        assertThat(milestoneRepository.getOpenMilestoneCount()).isEqualTo(1);
        assertThat(milestoneRepository.getClosedMilestoneCount()).isEqualTo(1);
        assertThat(milestoneRepository.findActiveMilestoneById(open.getId())).isPresent();
        assertThat(milestoneRepository.findActiveMilestoneById(deleted.getId())).isEmpty();
    }

    @Test
    void findsActiveMilestonesByIdsOnly() {
        Milestone first = milestoneRepository.save(milestone("First"));
        Milestone second = milestoneRepository.save(milestone("Second"));
        Milestone deleted = milestoneRepository.save(milestone("Deleted"));
        milestoneRepository.deleteMilestoneById(deleted.getId());

        List<Milestone> found = milestoneRepository.findActiveAllByIds(List.of(first.getId(), second.getId(), deleted.getId()));

        assertThat(found)
                .extracting(Milestone::getId)
                .containsExactlyInAnyOrder(first.getId(), second.getId())
                .doesNotContain(deleted.getId());
    }
}
