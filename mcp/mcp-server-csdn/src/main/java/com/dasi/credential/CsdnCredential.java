package com.dasi.credential;

import lombok.*;
import org.springframework.util.StringUtils;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CsdnCredential {

    private String cookie;
    private String categories;
    private String tags;
    private String coverUrl;
    private String userId;

    public boolean checkValid() {
        return StringUtils.hasText(cookie)
                && StringUtils.hasText(categories)
                && StringUtils.hasText(tags)
                && StringUtils.hasText(coverUrl)
                && StringUtils.hasText(userId);
    }

}
