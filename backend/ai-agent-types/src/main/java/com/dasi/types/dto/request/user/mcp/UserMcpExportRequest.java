package com.dasi.types.dto.request.user.mcp;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserMcpExportRequest {

    @NotBlank
    private String mcpId;
}
