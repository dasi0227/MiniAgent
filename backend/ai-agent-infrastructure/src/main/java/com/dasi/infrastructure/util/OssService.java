package com.dasi.infrastructure.util;

import com.aliyun.oss.OSS;
import com.dasi.domain.util.jwt.UserContext;
import com.dasi.domain.util.oss.IOssService;
import com.dasi.types.exception.MiniAgentException;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.util.Locale;

import static com.dasi.types.constant.ExceptionMessage.*;

@Slf4j
@Service
public class OssService implements IOssService {

    @Value("${oss.bucket}")
    private String bucketName;

    @Value("${oss.endpoint}")
    private String endpoint;

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
        ossClient.deleteObject(bucketName, objectName);
    }

    @Override
    public String uploadObject(MultipartFile file) {
        String extensionName = getExtensionName(file);
        try {
            String objectName = createObjectName(extensionName);
            ossClient.putObject(bucketName, objectName, new ByteArrayInputStream(file.getBytes()));
            return objectName;
        } catch (Exception e) {
            throw new MiniAgentException(AVATAR_UPLOAD_FAIL);
        }
    }

    private static String getExtensionName(MultipartFile file) {
        if (file == null) {
            throw new MiniAgentException(AVATAR_NOT_LEGAL);
        }

        if (file.getSize() > 1024 * 1024) {
            throw new MiniAgentException(AVATAR_SIZE_NOT_ALLOW);
        }

        String fileName = file.getOriginalFilename();
        if (fileName == null || !fileName.contains(".")) {
            throw new MiniAgentException(AVATAR_NOT_LEGAL);
        }

        String extensionName = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase(Locale.ROOT);
        if (!extensionName.equals("png") && !extensionName.equals("jpg")) {
            throw new MiniAgentException(AVATAR_EXTENSION_NOT_ALLOW);
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
        return "https://" + bucketName + "." + endpoint + "/" + objectName;
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
