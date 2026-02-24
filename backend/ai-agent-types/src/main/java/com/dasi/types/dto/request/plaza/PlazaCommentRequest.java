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
public class PlazaCommentRequest {

    @NotBlank
    private String plazaId;

    @NotBlank
    private String commentContent;

}
