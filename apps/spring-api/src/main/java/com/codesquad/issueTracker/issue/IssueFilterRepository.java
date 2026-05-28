package com.codesquad.issueTracker.issue;

import com.codesquad.issueTracker.issue.dto.request.IssueSearchCondition;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.SingleColumnRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class IssueFilterRepository {

    @Value("${app.issue-filter.paging-size}")
    private int pageSize;

    private final NamedParameterJdbcTemplate template;

    private final String idListQuery = "SELECT DISTINCT iss.id FROM issues AS iss " +
            "WHERE iss.status = :status";
    private final String countQuery = "SELECT COUNT(DISTINCT iss.id) FROM issues AS iss " +
            "WHERE iss.status = :status";

    public List<Long> filterIssueWithSearchCondition(IssueSearchCondition conditions){
        StringBuilder builder = new StringBuilder();
        builder.append(idListQuery);
        MapSqlParameterSource source = new MapSqlParameterSource();
        source.addValue("status", conditions.status().name());
        addConditionsValues(conditions, builder, source);
        builder.append(" ORDER BY iss.id DESC");
        builder.append(" LIMIT :pageSize");
        builder.append(" OFFSET :offset");
        source.addValue("pageSize", pageSize);
        source.addValue("offset", conditions.pageNumber() * pageSize);
        return template.query(builder.toString(),source, SingleColumnRowMapper.newInstance(Long.class));
    }

    public Long countIssuesUnderConditionAndStatus( IssueStatus status, IssueSearchCondition conditions){
        StringBuilder builder = new StringBuilder();
        builder.append(countQuery);
        MapSqlParameterSource source = new MapSqlParameterSource();
        source.addValue("status", status.name());
        addConditionsValues(conditions, builder, source);
        Long count = template.queryForObject(builder.toString(),source, Long.class);
        return count != null ? count : 0L;
    }


    private void addConditionsValues(IssueSearchCondition conditions ,StringBuilder builder, MapSqlParameterSource source){
        if(conditions.milestoneId() != null){
            builder.append(" AND iss.milestone_id = :milestoneId");
            source.addValue("milestoneId", conditions.milestoneId());
        }
        if(conditions.authorId() != null){
            builder.append(" AND iss.author_id = :authorId");
            source.addValue("authorId", conditions.authorId());
        }
        if(conditions.assigneeIds() != null && !conditions.assigneeIds().isEmpty()){
            List<Long> assigneeIds = conditions.assigneeIds().stream().distinct().toList();
            builder.append(" AND iss.id IN (SELECT iu.issue_id FROM issue_users as iu WHERE iu.user_id IN (:assigneeIds)");
            builder.append(" GROUP BY iu.issue_id HAVING COUNT(DISTINCT iu.user_id) = :assigneeCount)");
            source.addValue("assigneeIds",assigneeIds);
            source.addValue("assigneeCount", assigneeIds.size());
        }
        if(conditions.labelIds() != null && !conditions.labelIds().isEmpty()){
            List<Long> labelIds = conditions.labelIds().stream().distinct().toList();
            builder.append(" AND iss.id IN (SELECT il.issue_id FROM issue_labels AS il WHERE il.label_id IN (:labelIds)");
            builder.append(" GROUP BY il.issue_id HAVING COUNT(DISTINCT il.label_id) = :labelCount)");
            source.addValue("labelIds",labelIds);
            source.addValue("labelCount", labelIds.size());
        }
    }
}
