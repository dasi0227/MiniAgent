package com.dasi.api;

import com.dasi.types.dto.request.studio.*;
import com.dasi.types.dto.response.studio.StudioAgentResponse;
import com.dasi.types.dto.response.studio.StudioGenerateResponse;
import com.dasi.types.dto.result.Result;

import java.util.List;

public interface IStudioApi {

    Result<StudioGenerateResponse> generate(StudioGenerateRequest request);

    Result<StudioAgentResponse> create(StudioCreateRequest request);

    Result<StudioAgentResponse> update(StudioUpdateRequest request);

    Result<StudioAgentResponse> detail(String agentId);

    Result<List<StudioAgentResponse>> listMine();
}
