package com.dasi.infrastructure.persistent.po;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/** 接口配置表 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AiApi {
    /** 自增 id */
    private Long id;

    /** 接口 id */
    private String apiId;

    /** 基础路径 */
    private String apiBaseUrl;

    /** 密钥 */
    @Builder.Default
    private String apiKey = "";

    /** 对话路径 */
    @Builder.Default
    private String apiCompletionsPath = "v1/chat/completions";

    /** 嵌入路径 */
    @Builder.Default
    private String apiEmbeddingsPath = "v1/embeddings";

    /** 归属用户 id：0-系统，其它-用户 id */
    private Long apiFrom;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;

}
