package com.dasi.domain.ai.service.rag;

import com.dasi.domain.ai.model.dto.AiUploadDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface IRagService {

    void uploadTextFile(String ragTag, List<MultipartFile> fileList);

    void uploadGitRepo(AiUploadDTO aiUploadDTO);

}
