package com.dasi.domain.studio.repository;

import com.dasi.types.dto.request.studio.StudioCreateRequest;
import com.dasi.types.dto.request.studio.StudioUpdateRequest;
import com.dasi.types.dto.response.studio.StudioAgentResponse;
import com.dasi.types.dto.response.studio.StudioGenerateResponse;

import java.util.List;

public interface IStudioRepository {

    StudioGenerateResponse generate(Long userId, String taskPrompt, String strategy, List<String> mcpIdList);

    StudioAgentResponse create(Long userId, StudioCreateRequest request);

    StudioAgentResponse update(Long userId, StudioUpdateRequest request);

    StudioAgentResponse detail(Long userId, String agentId);

    List<StudioAgentResponse> listMine(Long userId);
}
