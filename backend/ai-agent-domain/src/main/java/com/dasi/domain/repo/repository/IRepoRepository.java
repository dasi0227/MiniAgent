package com.dasi.domain.repo.repository;

import com.dasi.types.dto.response.repo.RepoItemResponse;

import java.util.List;

public interface IRepoRepository {

    List<RepoItemResponse> list(Long userId);

    void add(Long userId, String agentId);

    void remove(Long userId, String agentId);

    void fork(Long userId, String plazaId);
}
