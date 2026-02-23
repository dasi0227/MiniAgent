package com.dasi.domain.util.oss;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "oss", ignoreInvalidFields = true)
public class OssProperties {

    private String endpoint;
    private String bucket;
    private String accessKeyId;
    private String accessKeySecret;

}
