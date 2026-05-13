package com.codesquad.issueTracker.label;

import com.codesquad.issueTracker.common.exception.BusinessException;
import com.codesquad.issueTracker.common.exception.ErrorCode;
import com.codesquad.issueTracker.label.dto.LabelRequest;
import com.codesquad.issueTracker.label.dto.LabelResponse;
import com.codesquad.issueTracker.label.dto.LabelsResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class LabelService {
    private final LabelRepository labelRepository;

    @Transactional(readOnly = true)
    public LabelsResponse findLabels() {
        List<Label> labels = labelRepository.findAll();
        return LabelsResponse.from(labels);
    }

    @Transactional(readOnly = true)
    public LabelResponse findLabelById(Long id) {
        Label label = findById(id);
        return LabelResponse.from(label);
    }

    public LabelResponse create(LabelRequest request) {
        Label label = request.toEntity();
        Label savedLabel = labelRepository.save(label);
        return LabelResponse.from(savedLabel);
    }

    public LabelResponse update(Long id, LabelRequest request) {
        Label label = findById(id);
        label.update(request.name(), request.description(), request.backgroundColor(), request.textColor());

        Label savedlabel = labelRepository.save(label);

        return LabelResponse.from(savedlabel);
    }

    public void delete(Long id) {
        Label label = findById(id);
        labelRepository.delete(label);
    }

    private Label findById(Long id) {
        return labelRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.LABEL_NOT_FOUND));
    }

}
