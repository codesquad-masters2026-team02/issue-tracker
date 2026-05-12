package com.codesquad.issueTracker.label;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("labels")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Label {
    @Id
    private Long labelId;
    private String name;
    private String description;
    private String backgroundColor;
    private TextColor textColor;
}
