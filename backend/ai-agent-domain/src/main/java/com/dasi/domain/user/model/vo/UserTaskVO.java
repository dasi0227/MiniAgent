package com.dasi.domain.user.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserTaskVO {

    private String taskId;

    private String agentId;

    private String taskCron;

    private String taskDesc;

    private String taskParam;

    private Integer taskStatus;

    private LocalDateTime updateTime;
}
