package com.dasi.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "miniagent.embedding", ignoreInvalidFields = true)
public class EmbeddingProperties {

    private String baseUrl;

    private String apiKey;

    private Integer dimensions;

    private String model;

    private String schemaName;

    private String tableName;

    private String encodingFormat;

}
