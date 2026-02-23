package com.dasi.config;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.dasi.domain.util.oss.OssProperties;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class OssClientConfig {

    @Resource
    private OssProperties aliOssProperties;

    @Bean
    public OSS ossClient() {
        log.info("【初始化配置】OssClient");
        return new OSSClientBuilder().build(
                aliOssProperties.getEndpoint(),
                aliOssProperties.getAccessKeyId(),
                aliOssProperties.getAccessKeySecret()
        );
    }

}
