package com.dasi.api;

import com.dasi.types.dto.request.plaza.*;
import com.dasi.types.dto.response.plaza.PlazaDetailResponse;
import com.dasi.types.dto.response.plaza.PlazaItemResponse;
import com.dasi.types.dto.result.PageResult;
import com.dasi.types.dto.result.Result;

public interface IPlazaApi {

    Result<PageResult<PlazaItemResponse>> list(PlazaListRequest request);

    Result<PlazaDetailResponse> detail(String plazaId);

    Result<Void> publish(PlazaPublishRequest request);

    Result<Void> like(PlazaActionRequest request);

    Result<Void> favor(PlazaActionRequest request);

    Result<Void> comment(PlazaCommentRequest request);
}
