package com.codesquad.issueTracker.issue;

import java.util.List;

import com.codesquad.issueTracker.milestone.dto.MilestoneIssueCountDTO;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface IssueRepository extends ListCrudRepository<Issue, Long> {
    List<Issue> findByStatus(IssueStatus status);

    long countByStatus(IssueStatus status);

    @Query("SELECT COUNT(*) FROM ISSUES WHERE milestone_id = :milestoneId AND STATUS = 'OPEN'")
    long countOpenIssuesByMilestoneId(@Param("milestoneId") Long milestoneId);

    @Query("SELECT COUNT(*) FROM ISSUES WHERE milestone_id = :milestoneId AND STATUS = 'CLOSED'")
    long countClosedIssuesByMilestoneId(@Param("milestoneId") Long milestoneId);

    @Query("""
        SELECT milestone_id,
               SUM(CASE WHEN status = 'OPEN' THEN 1 ELSE 0 END) AS open_issue_count,
               SUM(CASE WHEN status = 'CLOSED' THEN 1 ELSE 0 END) AS closed_issue_count
        FROM ISSUES
        WHERE milestone_id IS NOT NULL
        GROUP BY milestone_id
    """)
    List<MilestoneIssueCountDTO> countAllMilestonesIssueCounts();

    @Modifying
    @Query("UPDATE ISSUES SET milestone_id = NULL WHERE milestone_id = :milestoneId")
    long updateMilestoneDeletion(@Param("milestoneId") Long milestoneId);
}
