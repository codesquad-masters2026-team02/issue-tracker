package com.codesquad.issueTracker.milestone;

import com.codesquad.issueTracker.common.response.ApiResponse;
import com.codesquad.issueTracker.milestone.dto.MilestoneListResponse;
import com.codesquad.issueTracker.milestone.dto.MilestoneRequest;
import com.codesquad.issueTracker.milestone.dto.MilestoneResponse;
import com.codesquad.issueTracker.milestone.dto.MilestoneStatusUpdateRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/milestones")
public class MilestoneController {
    private final MilestoneService service;

    @PostMapping
    public ResponseEntity<ApiResponse<MilestoneResponse>> postNewMilestone(@Valid @RequestBody MilestoneRequest request){
        MilestoneResponse newMilestone = service.postNewMilestone(request);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(newMilestone.id())
                .toUri();

        return ResponseEntity.created(location).body(ApiResponse.ok(newMilestone));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<MilestoneListResponse>> getMilestoneList(){
        MilestoneListResponse milestones = service.getAllMilestones();
        return ResponseEntity.ok(ApiResponse.ok(milestones));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteMilestone(@PathVariable Long id){
        service.deleteMilestoneById(id);
        return ResponseEntity.ok(ApiResponse.noContent());
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<MilestoneResponse>> updateMilestone(@PathVariable Long id, @Valid @RequestBody MilestoneRequest request){
        MilestoneResponse response = service.updateMilestoneById(id, request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<MilestoneResponse>> updateMilestoneStatus(@PathVariable Long id, @Valid @RequestBody MilestoneStatusUpdateRequest request){
        MilestoneResponse response = service.changeMilestoneStatusById(id, request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
