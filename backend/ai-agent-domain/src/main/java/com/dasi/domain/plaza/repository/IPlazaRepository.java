package com.dasi.domain.plaza.repository;

import com.dasi.types.dto.request.plaza.*;
import com.dasi.types.dto.response.plaza.PlazaDetailResponse;
import com.dasi.types.dto.response.plaza.PlazaItemResponse;
import com.dasi.types.dto.result.PageResult;

public interface IPlazaRepository {

    PageResult<PlazaItemResponse> list(Long userId, PlazaListRequest request);

    PlazaDetailResponse detail(Long userId, String plazaId);

    void publish(Long userId, PlazaPublishRequest request);

    void like(Long userId, String plazaId);

    void favor(Long userId, String plazaId);

    void comment(Long userId, PlazaCommentRequest request);
}
