package com.dasi.domain.admin.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TaskManageDTO {

    @NotBlank
    private String taskId;

    @NotBlank
    private String agentId;

    @NotBlank
    private String taskCron;

    @NotBlank
    private String taskDesc;

    @NotBlank
    private String taskParam;

    @NotNull
    private Integer taskStatus;
}
