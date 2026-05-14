package com.codesquad.issueTracker.milestone;

import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MilestoneRepository extends ListCrudRepository<Milestone, Long> {

    @Query("SELECT COUNT(*) FROM MILESTONES WHERE status IN ('closed' , 'CLOSED')")
    int getClosedMilestoneCount();

    @Query("SELECT COUNT(*) FROM MILESTONES WHERE status IN ('open' , 'OPEN')")
    int getOpenMilestoneCount();

    @Modifying
    @Query("DELETE FROM MILESTONES WHERE id = :id")
    int deleteMilestoneById(@Param("id") Long id);
}
