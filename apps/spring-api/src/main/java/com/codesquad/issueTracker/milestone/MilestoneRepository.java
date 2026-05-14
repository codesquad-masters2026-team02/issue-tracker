package com.codesquad.issueTracker.milestone;

import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MilestoneRepository extends ListCrudRepository<Milestone, Long> {

    @Query("SELECT COUNT(*) FROM MILESTONES WHERE status = 'CLOSED' AND is_deleted = false")
    int getClosedMilestoneCount();

    @Query("SELECT COUNT(*) FROM MILESTONES WHERE status = 'OPEN' AND is_deleted = false")
    int getOpenMilestoneCount();

    @Modifying
    @Query("UPDATE MILESTONES SET is_deleted = true WHERE id = :id")
    int deleteMilestoneById(@Param("id") Long id);

    @Query("SELECT * FROM MILESTONES WHERE is_deleted = false")
    List<Milestone> findAllActive();
}
