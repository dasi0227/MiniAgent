package com.dasi.types.dto.request.user.mcp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserMcpListRequest {

    private String idKeyword;

    private String nameKeyword;
}
