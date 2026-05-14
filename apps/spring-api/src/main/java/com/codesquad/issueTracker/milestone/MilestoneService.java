package com.codesquad.issueTracker.milestone;

import com.codesquad.issueTracker.common.exception.BusinessException;
import com.codesquad.issueTracker.common.exception.ErrorCode;
import com.codesquad.issueTracker.milestone.dto.MilestoneListResponse;
import com.codesquad.issueTracker.milestone.dto.MilestoneRequest;
import com.codesquad.issueTracker.milestone.dto.MilestoneResponse;
import com.codesquad.issueTracker.milestone.dto.MilestoneStatusUpdateRequest;
import jakarta.servlet.Servlet;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MilestoneService {

    private final MilestoneRepository repo;

    public MilestoneResponse postNewMilestone(MilestoneRequest request){
        Milestone requestMilestone = request.toEntity();
        Milestone savedMilestone = repo.save(requestMilestone);
        return new MilestoneResponse(savedMilestone);
    }

    public MilestoneListResponse getAllMilestones(){
        List<Milestone> milestoneList = repo.findAllActive();
        int openMilestoneCount = repo.getOpenMilestoneCount();
        int closedMilestoneCount = repo.getClosedMilestoneCount();
        return new MilestoneListResponse(milestoneList,openMilestoneCount,closedMilestoneCount);
    }

    public void deleteMilestoneById(Long id){
        if(repo.deleteMilestoneById(id) == 0){
            throw new BusinessException(ErrorCode.MILESTONE_NOT_FOUND);
        }
    }

    public MilestoneResponse updateMilestoneById(Long id, MilestoneRequest request){
        Milestone targetMilestone = repo.findById(id).orElseThrow(()->new BusinessException(ErrorCode.MILESTONE_NOT_FOUND));
        targetMilestone.update(request.name(),request.dueDate(),request.description());
        Milestone savedMilestone = repo.save(targetMilestone);
        return new MilestoneResponse(savedMilestone);
    }

    public MilestoneResponse changeMilestoneStatusById(Long id, MilestoneStatusUpdateRequest request){
        Milestone targetMilestone = repo.findById(id).orElseThrow(() -> new BusinessException(ErrorCode.MILESTONE_NOT_FOUND));
        targetMilestone.changeStatus(request.status());
        Milestone savedMilestone = repo.save(targetMilestone);
        return new MilestoneResponse(savedMilestone);
    }

}
