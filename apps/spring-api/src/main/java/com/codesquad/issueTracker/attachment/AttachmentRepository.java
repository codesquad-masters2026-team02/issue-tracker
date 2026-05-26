package com.codesquad.issueTracker.attachment;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AttachmentRepository extends ListCrudRepository<Attachment, UUID> {

    @Query("SELECT * FROM attachments WHERE comment_id IN (:commentIds)")
    List<Attachment> findAllByCommentIdIn(@Param("commentIds") Collection<Long> commentIds);
}
