package com.dasi.domain.admin.model.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentPageDTO {

    private String keyword;

    private String agentType;

    @NotNull
    @Min(1)
    private Integer pageNum;

    @NotNull
    @Min(1)
    @Max(10)
    private Integer pageSize;
}
