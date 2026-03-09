package com.dasi.credential;

import lombok.*;
import org.springframework.util.StringUtils;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailCredential {

    private String smtpHost;
    private Integer smtpPort;
    private String smtpUsername;
    private String smtpPassword;
    private String fromAddress;
    private String fromName;
    private String userId;

    public boolean checkValid() {
        return StringUtils.hasText(smtpHost)
                && smtpPort != null
                && StringUtils.hasText(smtpUsername)
                && StringUtils.hasText(smtpPassword)
                && StringUtils.hasText(fromAddress)
                && StringUtils.hasText(fromName)
                && StringUtils.hasText(userId);
    }

}
