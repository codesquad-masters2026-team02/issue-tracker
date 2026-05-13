package com.codesquad.issueTracker.label;

import com.codesquad.issueTracker.common.response.ApiResponse;
import com.codesquad.issueTracker.label.dto.LabelRequest;
import com.codesquad.issueTracker.label.dto.LabelResponse;
import com.codesquad.issueTracker.label.dto.LabelsResponse;
import jakarta.validation.Valid;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/labels")
@RequiredArgsConstructor
public class LabelController {
    private final LabelService labelService;

    @GetMapping
    public ResponseEntity<ApiResponse<LabelsResponse>> getLabels() {
        LabelsResponse labels = labelService.findLabels();
        return ResponseEntity.ok(ApiResponse.ok(labels));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<LabelResponse>> getLabel(@PathVariable Long id) {
        LabelResponse label = labelService.findLabelById(id);
        return ResponseEntity.ok(ApiResponse.ok(label));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<LabelResponse>> createLabel(@Valid @RequestBody LabelRequest request) {
        LabelResponse created = labelService.create(request);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.labelId())
                .toUri();

        return ResponseEntity
                .created(location)
                .body(ApiResponse.ok(created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<LabelResponse>> updateLabel(
            @PathVariable Long id,
            @Valid @RequestBody LabelRequest request
    ) {
        LabelResponse updated = labelService.update(id, request);

        return ResponseEntity.ok(ApiResponse.ok(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteLabel(@PathVariable Long id) {
        labelService.delete(id);

        return ResponseEntity.ok(ApiResponse.noContent());
    }
}
