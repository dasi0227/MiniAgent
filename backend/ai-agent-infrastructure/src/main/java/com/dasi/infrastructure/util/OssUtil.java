package com.dasi.infrastructure.util;

import com.aliyun.oss.OSS;
import com.dasi.domain.util.jwt.UserContext;
import com.dasi.domain.util.oss.IOssUtil;
import com.dasi.domain.util.oss.OssProperties;
import com.dasi.types.exception.WorkException;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.util.Locale;

import static com.dasi.types.constant.ExceptionMessage.*;

@Slf4j
@Service
public class OssUtil implements IOssUtil {

    @Resource
    private OssProperties ossProperties;

    @Resource
    private OSS ossClient;

    @Resource
    private UserContext userContext;

    @Override
    public void deleteObject(String objectName) {
        objectName = normalizeObjectName(objectName);
        if (objectName.isEmpty()) {
            return;
        }
        ossClient.deleteObject(ossProperties.getBucket(), objectName);
    }

    @Override
    public String uploadObject(MultipartFile file) {
        String extensionName = getExtensionName(file);
        try {
            String objectName = createObjectName(extensionName);
            ossClient.putObject(ossProperties.getBucket(), objectName, new ByteArrayInputStream(file.getBytes()));
            return objectName;
        } catch (Exception e) {
            log.error("【OSS】上传服务错误", e);
            throw new WorkException(AVATAR_UPLOAD_FAIL);
        }
    }

    private static String getExtensionName(MultipartFile file) {
        if (file == null) {
            throw new WorkException(AVATAR_NOT_LEGAL);
        }

        if (file.getSize() > 1024 * 1024) {
            throw new WorkException(AVATAR_SIZE_NOT_ALLOW);
        }

        String fileName = file.getOriginalFilename();
        if (fileName == null || !fileName.contains(".")) {
            throw new WorkException(AVATAR_NOT_LEGAL);
        }

        String extensionName = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase(Locale.ROOT);
        if (!extensionName.equals("png") && !extensionName.equals("jpg")) {
            throw new WorkException(AVATAR_EXTENSION_NOT_ALLOW);
        }

        return extensionName;
    }

    @Override
    public String getObjectUrl(String objectName) {
        if (objectName == null || objectName.isEmpty()) {
            return "";
        }
        if (objectName.startsWith("http://") || objectName.startsWith("https://")) {
            return objectName;
        }
        return "https://" + ossProperties.getBucket() + "." + ossProperties.getEndpoint() + "/" + objectName;
    }

    private String createObjectName(String extensionName) {
        return userContext.getUserName() + "_" + System.currentTimeMillis() + "." + extensionName;
    }

    private String normalizeObjectName(String objectName) {
        if (objectName == null || objectName.isBlank()) {
            return "";
        }

        String normalized = objectName.trim();
        int queryIndex = normalized.indexOf('?');
        if (queryIndex >= 0) {
            normalized = normalized.substring(0, queryIndex);
        }

        if (normalized.startsWith("http://") || normalized.startsWith("https://")) {
            int slashIndex = normalized.lastIndexOf('/');
            if (slashIndex >= 0 && slashIndex + 1 < normalized.length()) {
                normalized = normalized.substring(slashIndex + 1);
            }
        }

        return normalized;
    }

}
