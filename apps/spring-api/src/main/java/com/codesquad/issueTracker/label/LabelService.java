package com.codesquad.issueTracker.label;

import com.codesquad.issueTracker.common.exception.BusinessException;
import com.codesquad.issueTracker.common.exception.ErrorCode;
import com.codesquad.issueTracker.label.dto.LabelRequest;
import com.codesquad.issueTracker.label.dto.LabelResponse;
import com.codesquad.issueTracker.label.dto.LabelsResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LabelService {
    private final LabelRepository labelRepository;

    public LabelsResponse findLabels() {
        List<Label> labels = labelRepository.findAll();
        return LabelsResponse.from(labels);
    }

    public LabelResponse findLabelById(Long id) {
        Label label = labelRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.LABEL_NOT_FOUND));

        return LabelResponse.from(label);
    }

    public LabelResponse create(LabelRequest request) {
        Label label = request.toEntity();
        Label savedLabel = labelRepository.save(label);
        return LabelResponse.from(savedLabel);
    }

}
