package com.dasi.types.dto.request.studio;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudioGenerateRequest {

    @NotBlank
    private String taskPrompt;

    @NotBlank
    private String strategy;

    private List<String> mcpIdList;
}
