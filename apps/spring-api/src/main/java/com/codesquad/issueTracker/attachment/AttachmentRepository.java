package com.codesquad.issueTracker.attachment;

import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AttachmentRepository extends ListCrudRepository<Attachment, Long> {

}
