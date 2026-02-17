package com.dasi.types.dto.response.plaza;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlazaDetailResponse {

    private PlazaItemResponse plazaItem;

    private List<PlazaCommentResponse> commentList;
}
