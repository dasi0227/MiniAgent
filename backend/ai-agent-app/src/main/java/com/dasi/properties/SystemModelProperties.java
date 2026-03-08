package com.dasi.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "system-model", ignoreInvalidFields = true)
public class SystemModelProperties {

    private String baseUrl;

    private String apiKey;

    private String model;

}
