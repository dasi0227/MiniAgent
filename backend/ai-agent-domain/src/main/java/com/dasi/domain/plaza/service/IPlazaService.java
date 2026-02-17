package com.dasi.domain.plaza.service;

import com.dasi.types.dto.request.plaza.*;
import com.dasi.types.dto.response.plaza.PlazaDetailResponse;
import com.dasi.types.dto.response.plaza.PlazaItemResponse;
import com.dasi.types.dto.result.PageResult;

public interface IPlazaService {

    PageResult<PlazaItemResponse> list(PlazaListRequest request);

    PlazaDetailResponse detail(String plazaId);

    void publish(PlazaPublishRequest request);

    void like(PlazaActionRequest request);

    void favor(PlazaActionRequest request);

    void comment(PlazaCommentRequest request);
}
