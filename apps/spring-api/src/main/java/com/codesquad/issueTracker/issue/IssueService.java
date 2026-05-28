package com.codesquad.issueTracker.issue;

import com.codesquad.issueTracker.comment.CommentService;
import com.codesquad.issueTracker.comment.CommentType;
import com.codesquad.issueTracker.comment.dto.CommentRequest;
import com.codesquad.issueTracker.common.exception.BusinessException;
import com.codesquad.issueTracker.common.exception.ErrorCode;
import com.codesquad.issueTracker.issue.dto.request.*;
import com.codesquad.issueTracker.issue.dto.response.AssigneeSummaryResponse;
import com.codesquad.issueTracker.issue.dto.response.IssueDetailResponse;
import com.codesquad.issueTracker.issue.dto.response.IssueSearchResponse;
import com.codesquad.issueTracker.issue.dto.response.IssueSummaryResponse;
import com.codesquad.issueTracker.label.Label;
import com.codesquad.issueTracker.label.LabelRepository;
import com.codesquad.issueTracker.label.dto.LabelSummaryResponse;
import com.codesquad.issueTracker.label.dto.LabelUpdateRequest;
import com.codesquad.issueTracker.milestone.Milestone;
import com.codesquad.issueTracker.milestone.MilestoneRepository;
import com.codesquad.issueTracker.milestone.dto.MilestoneSidebarUpdateRequest;
import com.codesquad.issueTracker.milestone.dto.MilestoneSummaryResponse;
import com.codesquad.issueTracker.user.User;
import com.codesquad.issueTracker.user.UserRepository;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class IssueService {
    private final IssueRepository issueRepository;
    private final LabelRepository labelRepository;
    private final MilestoneRepository milestoneRepository;
    private final CommentService commentService;
    private final IssueResponseMapper issueResponseMapper;
    private final UserRepository userRepository;
    private final IssueFilterRepository filterRepository;

    @Value("${app.issue-filter.paging-size}")
    private int pageSize;

    @Transactional
    public IssueDetailResponse create(IssueRequest request, Long authorId) {
        Issue issue = request.toEntity(authorId);
        Issue saved = issueRepository.save(issue);

        CommentRequest issueBodyRequest = new CommentRequest(request.content(), request.attachmentIds());
        commentService.postComment(saved.getId(), authorId, issueBodyRequest, CommentType.ISSUE_BODY);

        List<LabelSummaryResponse> labels = findLabelsByIssueLabels(saved);
        List<AssigneeSummaryResponse> assignees = findAssigneeByIssueUser(saved);

        MilestoneSummaryResponse milestone = null;
        if (saved.getMilestoneId() != null) {
            Milestone found = milestoneRepository.findActiveMilestoneById(saved.getMilestoneId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.MILESTONE_NOT_FOUND));
            milestone = MilestoneSummaryResponse.from(found, issueRepository.countOpenIssuesByMilestoneId(found.getId()), issueRepository.countClosedIssuesByMilestoneId(found.getId()) );
        }
        return IssueDetailResponse.from(saved, findUsername(authorId), labels, milestone, assignees);
    }

    public IssueSearchResponse getIssues(IssueSearchCondition condition) {
        long openIssueCount = filterRepository.countIssuesUnderConditionAndStatus(IssueStatus.OPEN,condition);
        long closedIssueCount = filterRepository.countIssuesUnderConditionAndStatus(IssueStatus.CLOSED, condition);

        long issueCount = condition.status() == IssueStatus.OPEN ? openIssueCount : closedIssueCount;
        int totalPages = (int) Math.ceil((double) issueCount / pageSize );

        if(totalPages > 0 && condition.pageNumber() >= totalPages){
            throw new BusinessException(ErrorCode.PAGE_NOT_FOUND);
        }

        List<Long> filteredIssueIds = filterRepository.filterIssueWithSearchCondition(condition);

        List<Issue> issues = issueRepository.findAllById(filteredIssueIds);
        Map<Long, Label> labelMap = findLabelMapByIssues(issues);
        Map<Long, Milestone> milestoneMap = findMilestoneMapByIssues(issues);
        Map<Long, Issue> issueMap = issues.stream().collect(Collectors.toMap(Issue::getId, Function.identity()));
        Map<Long, User> assigneeMap = findAssigneeMapByIssues(issues);
        Map<Long, User> authorMap = findAuthorMapByIssues(issues);

        List<IssueSummaryResponse> issueSummaryResponses = filteredIssueIds.stream()
                .map(issueMap::get)
                .filter(Objects::nonNull)
                .map(issue -> issueResponseMapper.toResponse(issue, authorMap, labelMap, milestoneMap, assigneeMap))
                .toList();


        boolean first = condition.pageNumber() == 0;
        boolean last = condition.pageNumber() >= totalPages - 1;

        return IssueSearchResponse.from(openIssueCount, closedIssueCount, condition.pageNumber(),pageSize,issueCount,totalPages,first,last, issueSummaryResponses);
    }

    public IssueDetailResponse findIssueById(Long id) {
        Issue issue = findById(id);
        List<LabelSummaryResponse> labels = findLabelsByIssueLabels(issue);
        List<AssigneeSummaryResponse> assignees = findAssigneeByIssueUser(issue);

        MilestoneSummaryResponse milestone = null;

        if (issue.getMilestoneId() != null) {
            Milestone found = milestoneRepository.findActiveMilestoneById(issue.getMilestoneId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.MILESTONE_NOT_FOUND));
            milestone = MilestoneSummaryResponse.from(found, issueRepository.countOpenIssuesByMilestoneId(found.getId()), issueRepository.countClosedIssuesByMilestoneId(found.getId()) );
        }

        return IssueDetailResponse.from(issue, findUsername(issue.getAuthorId()), labels, milestone, assignees);
    }

    @Transactional
    public void updateStatus(Long id, UpdateIssueStatusRequest request) {
        Issue issue = findById(id);
        issue.changeStatus(request.status());
        issueRepository.save(issue);
    }

    @Transactional
    public void updateTitle(Long id, IssueTitleUpdateRequest request) {
        Issue issue = findById(id);
        issue.updateTitle(request.title().trim());
        issueRepository.save(issue);
    }

    @Transactional
    public void bulkUpdateStatus(BulkIssueRequest request) {
        List<Issue> issues = issueRepository.findAllById(request.issueIds());

        for (Issue issue : issues) {
            issue.changeStatus(request.status());
        }
        issueRepository.saveAll(issues);
    }

    private Issue findById(Long id) {
        return issueRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.ISSUE_NOT_FOUND));
    }

    private String findUsername(Long userId) {
        if (userId == null) {
            return "알 수 없음";
        }

        return userRepository.findById(userId)
                .map(User::getUsername)
                .orElse("알 수 없음");
    }

    private List<LabelSummaryResponse> findLabelsByIssueLabels(Issue issue) {
        List<Long> labelIds = issue.getLabels().stream()
                .map(IssueLabel::labelId)
                .toList();
        return labelRepository.findAllById(labelIds).stream()
                .map(LabelSummaryResponse::from)
                .toList();
    }

    private Map<Long, Label> findLabelMapByIssues(List<Issue> issues) {
        Set<Long> labelIds = issues.stream()
                .flatMap(issue -> issue.getLabels().stream())
                .map(IssueLabel::labelId)
                .collect(Collectors.toSet());

        if (labelIds.isEmpty()) {
            return Map.of();
        }

        return labelRepository.findAllById(labelIds).stream()
                .collect(Collectors.toMap(Label::getId, Function.identity()));
    }


    private Map<Long, User> findAuthorMapByIssues(List<Issue> issues) {
        Set<Long> authorIds = issues.stream()
                .map(Issue::getAuthorId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        if (authorIds.isEmpty()) {
            return Map.of();
        }

        return userRepository.findAllById(authorIds).stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));
    }

    private Map<Long, Milestone> findMilestoneMapByIssues(List<Issue> issues) {
        Set<Long> milestoneIds = issues.stream()
                .map(Issue::getMilestoneId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        if (milestoneIds.isEmpty()) {
            return Map.of();
        }

        return milestoneRepository.findActiveAllByIds(milestoneIds).stream()
                .collect(Collectors.toMap(Milestone::getId, Function.identity()));
    }

    private Map<Long, User> findAssigneeMapByIssues(List<Issue> issues) {
        Set<Long> assigneeIds = issues.stream()
                .flatMap(issue -> issue.getUsers().stream())
                .map(IssueUser::userId)
                .collect(Collectors.toSet());

        if (assigneeIds.isEmpty()) {
            return Map.of();
        }

        return userRepository.findAllById(assigneeIds).stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));
    }

    private List<AssigneeSummaryResponse> findAssigneeByIssueUser(Issue issue){
        List<Long> userIds = issue.getUsers().stream().map(IssueUser::userId).toList();

        return userRepository.findAllById(userIds).stream().map(AssigneeSummaryResponse::from).toList();
    }

    @Transactional
    public void updateAssignees(Long issueId ,boolean isAssignment, AssigneeUpdateRequest request){
        Set<Long> requestedIds = new HashSet<>(request.userIds());
        List<User> targetUsers = userRepository.findAllById(requestedIds);

        if(requestedIds.size() != targetUsers.size()){
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        Issue targetIssue = issueRepository.findById(issueId).orElseThrow(()-> new BusinessException(ErrorCode.ISSUE_NOT_FOUND));

        if(isAssignment){
            targetUsers.forEach(u -> targetIssue.assignUser(u.getId()));
        }
        else{
            targetUsers.forEach(u -> targetIssue.unassignUser(u.getId()));
        }

        issueRepository.save(targetIssue);
    }

    @Transactional
    public void updateLabel(Long issueId, boolean isAssignment, LabelUpdateRequest request){
        Set<Long> labelIds = new HashSet<>(request.labelIds());
        List<Label> targetLabels = labelRepository.findAllById(labelIds);

        if(labelIds.size() != targetLabels.size()){
            throw new BusinessException(ErrorCode.LABEL_NOT_FOUND);
        }

        Issue targetIssue = issueRepository.findById(issueId).orElseThrow(()-> new BusinessException(ErrorCode.ISSUE_NOT_FOUND));

        if(isAssignment){
            targetLabels.forEach(u -> targetIssue.assignLabel(u.getId()));
        }
        else{
            targetLabels.forEach(u -> targetIssue.unassignLabel(u.getId()));
        }

        issueRepository.save(targetIssue);
    }

    @Transactional
    public void updateMilestone(Long issueId, boolean isAssignment, MilestoneSidebarUpdateRequest request){

        Issue targetIssue = issueRepository.findById(issueId).orElseThrow(() -> new BusinessException(ErrorCode.ISSUE_NOT_FOUND));

        if(request == null){
            if(!isAssignment) targetIssue.updateMilestone(null);
        }
        else{
            if(isAssignment){
                Long milestoneId = request.milestoneId();
                Milestone milestone = milestoneRepository.findActiveMilestoneById(milestoneId).orElseThrow(() -> new BusinessException(ErrorCode.MILESTONE_NOT_FOUND));
                targetIssue.updateMilestone(milestone.getId());
            }
        }

        issueRepository.save(targetIssue);
    }


}
