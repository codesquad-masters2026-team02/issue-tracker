package com.codesquad.issueTracker.label;

import static com.codesquad.issueTracker.TestFixtures.label;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.codesquad.issueTracker.common.exception.BusinessException;
import com.codesquad.issueTracker.common.exception.ErrorCode;
import com.codesquad.issueTracker.label.dto.LabelDetailResponse;
import com.codesquad.issueTracker.label.dto.LabelRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class LabelServiceTest {

    @Autowired
    LabelService labelService;

    @Autowired
    LabelRepository labelRepository;

    @Test
    void createsAndFindsLabel() {
        LabelDetailResponse created = labelService.create(
                new LabelRequest("bug", "Bug label", "#FF0000", TextColor.LIGHT)
        );

        LabelDetailResponse found = labelService.findLabelById(created.labelId());

        assertThat(found.name()).isEqualTo("bug");
        assertThat(found.description()).isEqualTo("Bug label");
        assertThat(found.backgroundColor()).isEqualTo("#FF0000");
        assertThat(found.textColor()).isEqualTo(TextColor.LIGHT);
    }

    @Test
    void updatesLabel() {
        Label saved = labelRepository.save(label("old"));

        LabelDetailResponse updated = labelService.update(
                saved.getId(),
                new LabelRequest("new", "Updated", "#00FF00", TextColor.DARK)
        );

        assertThat(updated.name()).isEqualTo("new");
        assertThat(updated.description()).isEqualTo("Updated");
        assertThat(updated.backgroundColor()).isEqualTo("#00FF00");
        assertThat(updated.textColor()).isEqualTo(TextColor.DARK);
    }

    @Test
    void deletesLabel() {
        Label saved = labelRepository.save(label("delete-me"));

        labelService.delete(saved.getId());

        assertThat(labelRepository.findById(saved.getId())).isEmpty();
    }

    @Test
    void findMissingLabelThrowsLabelNotFound() {
        assertThatThrownBy(() -> labelService.findLabelById(999_999L))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.LABEL_NOT_FOUND);
    }

    @Test
    void listsLabels() {
        Label first = labelRepository.save(label("first"));
        Label second = labelRepository.save(label("second"));

        assertThat(labelService.findLabels().labels())
                .extracting(LabelDetailResponse::labelId)
                .contains(first.getId(), second.getId());
    }
}
