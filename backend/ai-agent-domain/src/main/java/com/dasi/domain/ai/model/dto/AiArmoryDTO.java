package com.dasi.domain.ai.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AiArmoryDTO {
    
    @NotBlank
    private String armoryType;

    @NotBlank
    private String armoryId;

}
