package com.dasi.domain.studio.service;

import com.dasi.types.dto.request.studio.*;
import com.dasi.types.dto.response.studio.StudioAgentResponse;
import com.dasi.types.dto.response.studio.StudioGenerateResponse;

import java.util.List;

public interface IStudioService {

    StudioGenerateResponse generate(StudioGenerateRequest request);

    StudioAgentResponse create(StudioCreateRequest request);

    StudioAgentResponse update(StudioUpdateRequest request);

    StudioAgentResponse detail(String agentId);

    List<StudioAgentResponse> listMine();
}
