package com.codesquad.issueTracker.milestone;

import com.codesquad.issueTracker.milestone.dto.MilestoneListResponse;
import com.codesquad.issueTracker.milestone.dto.MilestoneRequest;
import com.codesquad.issueTracker.milestone.dto.MilestoneResponse;
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
        List<Milestone> milestoneList = repo.findAll();
        int openMilestoneCount = repo.getOpenMilestoneCount();
        int closedMilestoneCount = repo.getClosedMilestoneCount();
        return new MilestoneListResponse(milestoneList,openMilestoneCount,closedMilestoneCount);
    }
}
