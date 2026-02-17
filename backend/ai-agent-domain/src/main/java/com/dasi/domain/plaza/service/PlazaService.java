package com.dasi.domain.plaza.service;

import com.dasi.domain.plaza.repository.IPlazaRepository;
import com.dasi.domain.util.jwt.AuthContext;
import com.dasi.types.dto.request.plaza.*;
import com.dasi.types.dto.response.plaza.PlazaDetailResponse;
import com.dasi.types.dto.response.plaza.PlazaItemResponse;
import com.dasi.types.dto.result.PageResult;
import com.dasi.types.exception.AuthException;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

@Service
public class PlazaService implements IPlazaService {

    @Resource
    private IPlazaRepository plazaRepository;

    @Resource
    private AuthContext authContext;

    @Override
    public PageResult<PlazaItemResponse> list(PlazaListRequest request) {
        return plazaRepository.list(requiredUserId(), request);
    }

    @Override
    public PlazaDetailResponse detail(String plazaId) {
        return plazaRepository.detail(requiredUserId(), plazaId);
    }

    @Override
    public void publish(PlazaPublishRequest request) {
        plazaRepository.publish(requiredUserId(), request);
    }

    @Override
    public void like(PlazaActionRequest request) {
        plazaRepository.like(requiredUserId(), request.getPlazaId());
    }

    @Override
    public void favor(PlazaActionRequest request) {
        plazaRepository.favor(requiredUserId(), request.getPlazaId());
    }

    @Override
    public void comment(PlazaCommentRequest request) {
        plazaRepository.comment(requiredUserId(), request);
    }

    private Long requiredUserId() {
        Long userId = authContext.getId();
        if (userId == null) {
            throw new AuthException("未登录");
        }
        return userId;
    }
}
