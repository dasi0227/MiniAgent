package com.dasi.domain.miniagent.model.vo;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlazaDetailVO {

    private PlazaVO plaza;
    private List<PlazaCommentVO> commentList;

}
