package com.dasi.config;

import com.dasi.properties.EmbeddingProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.MetadataMode;
import org.springframework.ai.openai.OpenAiEmbeddingModel;
import org.springframework.ai.openai.OpenAiEmbeddingOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;

@Slf4j
@Configuration
@EnableConfigurationProperties(EmbeddingProperties.class)
public class PgVectorStoreConfig {

    @Bean
    @Primary
    public PgVectorStore pgVectorStore(EmbeddingProperties embeddingProperties, @Qualifier("postgresqlTemplate") JdbcTemplate jdbcTemplate) {

        log.info("【初始化配置】PgVectorStore");

        OpenAiApi openAiApi = OpenAiApi.builder()
                .baseUrl(embeddingProperties.getBaseUrl())
                .apiKey(embeddingProperties.getApiKey())
                .build();

        OpenAiEmbeddingOptions embeddingOptions = OpenAiEmbeddingOptions.builder()
                .model(embeddingProperties.getModel())
                .dimensions(embeddingProperties.getDimensions())
                .encodingFormat(embeddingProperties.getEncodingFormat())
                .build();

        OpenAiEmbeddingModel openAiEmbeddingModel = new OpenAiEmbeddingModel(openAiApi, MetadataMode.EMBED, embeddingOptions);

        return PgVectorStore.builder(jdbcTemplate, openAiEmbeddingModel)
                .initializeSchema(false)
                .schemaName(embeddingProperties.getSchemaName())
                .vectorTableName(embeddingProperties.getTableName())
                .build();
    }

    @Bean
    public TokenTextSplitter tokenTextSplitter() {
        log.info("【初始化配置】TokenTextSplitter");
        return new TokenTextSplitter();
    }

}
