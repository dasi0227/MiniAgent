package com.dasi.domain.admin.model.enumeration;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public enum AiAgentType {

    STEP("步骤规划", "step"),
    LOOP("循环执行", "loop"),
    REACT("逐步反应", "react")
    ;

    private String name;

    private String type;

}
