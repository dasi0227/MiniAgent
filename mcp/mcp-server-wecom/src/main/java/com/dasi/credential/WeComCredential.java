package com.dasi.credential;

import lombok.*;
import org.springframework.util.StringUtils;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WeComCredential {

    private String corpId;
    private String corpSecret;
    private Integer agentId;
    private String userId;

    public Boolean checkValid() {
        return StringUtils.hasText(this.corpId)
                && StringUtils.hasText(this.corpSecret)
                && StringUtils.hasText(this.userId)
                && agentId != null;
    }

}
