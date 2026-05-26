package com.codesquad.issueTracker.milestone;

import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MilestoneRepository extends ListCrudRepository<Milestone, Long> {

    @Query("SELECT COUNT(*) FROM milestones WHERE status = 'CLOSED' AND is_deleted = false")
    int getClosedMilestoneCount();

    @Query("SELECT COUNT(*) FROM milestones WHERE status = 'OPEN' AND is_deleted = false")
    int getOpenMilestoneCount();

    @Modifying
    @Query("UPDATE milestones SET is_deleted = true WHERE id = :id AND is_deleted = false")
    int deleteMilestoneById(@Param("id") Long id);

    @Query("SELECT * FROM milestones WHERE is_deleted = false")
    List<Milestone> findAllActive();

    @Override
    @Query("SELECT COUNT(1) FROM milestones WHERE is_deleted = false AND id = :id")
    boolean existsById(@Param("id") Long id);

    @Query("SELECT * FROM milestones WHERE is_deleted = false AND id = :id")
    Optional<Milestone> findActiveMilestoneById(@Param("id") Long id);

    @Query("SELECT * FROM milestones WHERE status = :status AND is_deleted = false")
    List<Milestone> findAllByStatus(@Param("status") MilestoneStatus status);

    @Query("SELECT * FROM milestones WHERE id IN (:ids) AND is_deleted = false")
    List<Milestone> findActiveAllByIds(@Param("ids") Iterable<Long> ids);
}
