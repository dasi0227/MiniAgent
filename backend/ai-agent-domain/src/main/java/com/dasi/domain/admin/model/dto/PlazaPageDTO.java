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
public class PlazaPageDTO {

    private String keyword;

    private String sortBy;

    private String sortOrder;

    @NotNull
    @Min(1)
    private Integer pageNum;

    @NotNull
    @Min(1)
    @Max(10)
    private Integer pageSize;

}
