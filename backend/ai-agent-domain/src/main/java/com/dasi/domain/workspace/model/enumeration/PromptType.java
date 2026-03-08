package com.dasi.domain.workspace.model.enumeration;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public enum PromptType {

    SYSTEM_PROMPT("用户提示词", "system-prompt"),
    USER_PROMPT("系统提示词", "user-prompt"),
    ;

    private String name;

    private String type;

}
