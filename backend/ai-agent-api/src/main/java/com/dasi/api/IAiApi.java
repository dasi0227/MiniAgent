package com.dasi.api;

import com.dasi.domain.ai.model.dto.AiArmoryDTO;
import com.dasi.domain.ai.model.dto.AiChatDTO;
import com.dasi.domain.ai.model.dto.AiWorkDTO;
import com.dasi.domain.ai.model.dto.AiUploadDTO;
import com.dasi.types.result.Result;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

import java.util.List;

public interface IAiApi {

    SseEmitter execute(AiWorkDTO aiWorkDTO);

    String complete(AiChatDTO aiChatDTO);

    Flux<String> stream(AiChatDTO aiChatDTO);

    Result<Void> armory(AiArmoryDTO aiArmoryDTO);

    Result<Void> uploadFile(String ragTag, List<MultipartFile> fileList);

    Result<Void> uploadGitRepo(AiUploadDTO aiUploadDTO);

}
