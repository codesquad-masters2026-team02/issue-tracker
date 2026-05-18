package com.codesquad.issueTracker.label;

import com.codesquad.issueTracker.common.exception.BusinessException;
import com.codesquad.issueTracker.common.exception.ErrorCode;
import com.codesquad.issueTracker.label.dto.LabelRequest;
import com.codesquad.issueTracker.label.dto.LabelDetailResponse;
import com.codesquad.issueTracker.label.dto.LabelListResponse;
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
    public LabelListResponse findLabels() {
        List<Label> labels = labelRepository.findAll();
        return LabelListResponse.from(labels);
    }

    @Transactional(readOnly = true)
    public LabelDetailResponse findLabelById(Long id) {
        Label label = findById(id);
        return LabelDetailResponse.from(label);
    }

    public LabelDetailResponse create(LabelRequest request) {
        Label label = request.toEntity();
        Label savedLabel = labelRepository.save(label);
        return LabelDetailResponse.from(savedLabel);
    }

    public LabelDetailResponse update(Long id, LabelRequest request) {
        Label label = findById(id);
        label.update(request.name(), request.description(), request.backgroundColor(), request.textColor());

        Label savedlabel = labelRepository.save(label);

        return LabelDetailResponse.from(savedlabel);
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
