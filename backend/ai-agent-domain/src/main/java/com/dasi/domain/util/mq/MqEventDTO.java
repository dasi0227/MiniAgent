package com.dasi.domain.util.mq;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MqEventDTO {

    private String plazaId;

    private Boolean liked;

    private Boolean favored;

    private String commentId;

    private String commentContent;

    private String templateId;

    private String agentId;

    private String plazaTitle;

    private String plazaDesc;

}
