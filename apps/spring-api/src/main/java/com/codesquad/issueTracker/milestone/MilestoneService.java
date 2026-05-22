package com.codesquad.issueTracker.milestone;

import com.codesquad.issueTracker.common.exception.BusinessException;
import com.codesquad.issueTracker.common.exception.ErrorCode;
import com.codesquad.issueTracker.issue.IssueRepository;
import com.codesquad.issueTracker.issue.IssueService;
import com.codesquad.issueTracker.milestone.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MilestoneService {

    private final MilestoneRepository repo;
    private final IssueRepository issueRepository;

    public MilestoneResponse postNewMilestone(MilestoneRequest request){
        Milestone requestMilestone = request.toEntity();
        Milestone savedMilestone = repo.save(requestMilestone);
        return new MilestoneResponse(savedMilestone, 0L, 0L);
    }

    public MilestoneListResponse getAllMilestonesByStatus(MilestoneStatus status){
        List<Milestone> milestoneList = repo.findAllByStatus(status);
        int openMilestoneCount = repo.getOpenMilestoneCount();
        int closedMilestoneCount = repo.getClosedMilestoneCount();
        Map<Long, MilestoneIssueCountDTO> milestoneIdCountDTOMap = issueRepository.countAllMilestonesIssueCounts().stream().collect(Collectors.toMap(MilestoneIssueCountDTO::milestoneId, d -> d));

        List<MilestoneResponse> responseList = milestoneList.stream().map(m -> {
            MilestoneIssueCountDTO countDTO = milestoneIdCountDTOMap.getOrDefault(m.getId(), new MilestoneIssueCountDTO(m.getId(), 0L , 0L));

            return new MilestoneResponse(m.getId()
                    ,m.getName()
                    ,m.getDescription()
                    ,m.getDueDate()
                    ,m.getStatus()
                    ,countDTO.openIssueCount()
                    ,countDTO.closedIssueCount());
        }).toList();

        return new MilestoneListResponse(responseList,openMilestoneCount,closedMilestoneCount);
    }

    @Transactional
    public void deleteMilestoneById(Long id){
        if(repo.deleteMilestoneById(id) == 0){
            throw new BusinessException(ErrorCode.MILESTONE_NOT_FOUND);
        }
        issueRepository.updateMilestoneDeletion(id);
    }

    public MilestoneResponse updateMilestoneById(Long id, MilestoneRequest request){
        Milestone targetMilestone = repo.findActiveMilestoneById(id).orElseThrow(()->new BusinessException(ErrorCode.MILESTONE_NOT_FOUND));
        targetMilestone.update(request.name(),request.dueDate(),request.description());
        Milestone savedMilestone = repo.save(targetMilestone);
        return new MilestoneResponse(savedMilestone, getOpenIssueCountByMilestoneId(savedMilestone.getId()), getClosedIssueCountByMilestoneId(savedMilestone.getId()));
    }

    public MilestoneResponse changeMilestoneStatusById(Long id, MilestoneStatusUpdateRequest request){
        Milestone targetMilestone = repo.findActiveMilestoneById(id).orElseThrow(() -> new BusinessException(ErrorCode.MILESTONE_NOT_FOUND));
        targetMilestone.changeStatus(request.status());
        Milestone savedMilestone = repo.save(targetMilestone);
        return new MilestoneResponse(savedMilestone, getOpenIssueCountByMilestoneId(savedMilestone.getId()), getClosedIssueCountByMilestoneId(savedMilestone.getId()));
    }

    public boolean findMilestoneExistenceById(Long id){
        return repo.existsById(id);
    }


    private Long getOpenIssueCountByMilestoneId(Long id){
        return issueRepository.countOpenIssuesByMilestoneId(id);
    }

    private Long getClosedIssueCountByMilestoneId(Long id){
        return issueRepository.countClosedIssuesByMilestoneId(id);
    }

}
