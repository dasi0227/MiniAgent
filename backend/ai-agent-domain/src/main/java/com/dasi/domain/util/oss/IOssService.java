package com.dasi.domain.util.oss;

import org.springframework.web.multipart.MultipartFile;

public interface IOssService {

    void deleteObject(String objectUrl);

    String uploadObject(MultipartFile file);

    String getObjectUrl(String objectName);
}
