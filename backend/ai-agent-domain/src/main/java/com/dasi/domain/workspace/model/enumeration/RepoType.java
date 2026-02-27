package com.dasi.domain.workspace.model.enumeration;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public enum RepoType {

    FAVOR("收藏", "favor"),
    SELF("自建", "self"),
    FORK("Fork", "fork");

    private String name;

    private String type;

}
