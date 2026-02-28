package com.dasi.domain.admin.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdvisorVO {
    private String advisorId;
    private String advisorName;
    private String advisorType;
    private String advisorParam;
    private LocalDateTime updateTime;
}
