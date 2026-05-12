package com.codesquad.issueTracker.label;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;

public class Label {
    @Id
    @Column("LABEL_ID")
    private Long id;

    @Column("NAME")
    private String name;

    @Column("DESCRIPTION")
    private String description;

    /** 포맷: # + 6자리 16진수 (예: #FFFFFF) */
    @Column("BACKGROUND_COLOR")
    private String backgroundColor;

    /** 텍스트 색상: (DARK: 어두운 색, LIGHT: 밝은 색) */
    @Column("TEXT_COLOR")
    private TextColor textColor;
}
