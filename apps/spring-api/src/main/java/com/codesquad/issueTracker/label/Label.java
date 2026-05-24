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
    private Long id;
    private String name;
    private String description;
    private String backgroundColor;
    private TextColor textColor;

    public void update(String name, String description, String backgroundColor, TextColor textColor) {
        this.name = name;
        this.description = description;
        this.backgroundColor = backgroundColor;
        this.textColor = textColor;
    }
}
