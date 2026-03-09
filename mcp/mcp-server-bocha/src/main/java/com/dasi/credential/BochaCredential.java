package com.dasi.credential;

import lombok.*;
import org.springframework.util.StringUtils;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BochaCredential {

    private String apiKey;
    private String userId;

    public boolean checkValid() {
        return StringUtils.hasText(apiKey) && StringUtils.hasText(userId);
    }

}
