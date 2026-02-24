package com.dasi.domain.user.model.vo.query;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChatMcpVO {

    private String mcpId;

    private String mcpName;

    private String mcpDesc;

}
