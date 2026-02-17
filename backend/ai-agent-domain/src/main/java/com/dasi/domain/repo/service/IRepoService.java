package com.dasi.domain.repo.service;

import com.dasi.types.dto.request.repo.RepoActionRequest;
import com.dasi.types.dto.request.repo.RepoForkRequest;
import com.dasi.types.dto.response.repo.RepoItemResponse;

import java.util.List;

public interface IRepoService {

    List<RepoItemResponse> list();

    void add(RepoActionRequest request);

    void remove(RepoActionRequest request);

    void fork(RepoForkRequest request);
}
