package com.dasi.domain.admin.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ConfigListDTO {

    private String idKeyword;

    private String valueKeyword;

    private String configType;

}