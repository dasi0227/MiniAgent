package com.dasi.infrastructure.persistent.po;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AiSecret {

    private Long id;

    private String secretId;

    private Long userId;

    private String secretScene;

    private String refType;

    private String refId;

    private String secretKey;

    private String secretCiphertext;

    private String secretNonce;

    private String secretAlgo;

    private String keyVersion;

    private Integer secretStatus;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
