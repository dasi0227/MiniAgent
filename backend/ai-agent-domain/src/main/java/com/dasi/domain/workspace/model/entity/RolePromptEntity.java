package com.dasi.domain.workspace.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RolePromptEntity {

    private String clientRole;

    private Integer flowSeq;

    private String systemPrompt;

    private String userPrompt;

}
