package com.dasi.domain.user.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserMcpVO {

    private Long id;

    private String mcpId;

    private String mcpName;

    private String mcpType;

    private String mcpDesc;

    private String mcpConfig;

    private String mcpSecret;

}
