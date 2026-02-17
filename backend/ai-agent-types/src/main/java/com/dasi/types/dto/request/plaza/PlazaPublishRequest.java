package com.dasi.types.dto.request.plaza;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlazaPublishRequest {

    @NotBlank
    private String agentId;

    @NotBlank
    private String plazaTitle;

    private String plazaDesc;
}
