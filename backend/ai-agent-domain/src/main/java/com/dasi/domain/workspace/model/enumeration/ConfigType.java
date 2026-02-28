package com.dasi.domain.workspace.model.enumeration;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public enum ConfigType {

    PROMPT("提示词", "prompt"),
    MCP("工具", "mcp");

    private String name;

    private String type;

}
