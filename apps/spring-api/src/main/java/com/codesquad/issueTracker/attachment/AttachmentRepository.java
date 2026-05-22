package com.codesquad.issueTracker.attachment;

import java.util.UUID;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AttachmentRepository extends ListCrudRepository<Attachment, UUID> {

}
