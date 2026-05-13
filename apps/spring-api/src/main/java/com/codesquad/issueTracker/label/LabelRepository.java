package com.codesquad.issueTracker.label;

import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LabelRepository extends ListCrudRepository<Label, Long> {

}
