package com.dasi.types.dto.request.plaza;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlazaListRequest {

    private String titleKeyword;

    @Builder.Default
    @Min(1)
    private Integer pageNum = 1;

    @Builder.Default
    @Min(1)
    @Max(50)
    private Integer pageSize = 10;
}
