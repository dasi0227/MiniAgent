package com.dasi.domain.util.oss;

import org.springframework.web.multipart.MultipartFile;

public interface IOssUtil {

    void deleteObject(String objectUrl);

    String uploadObject(MultipartFile file);

    String getObjectUrl(String objectName);
}
