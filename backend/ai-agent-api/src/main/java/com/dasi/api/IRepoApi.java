package com.dasi.api;

import com.dasi.types.dto.request.repo.RepoActionRequest;
import com.dasi.types.dto.request.repo.RepoForkRequest;
import com.dasi.types.dto.response.repo.RepoItemResponse;
import com.dasi.types.dto.result.Result;

import java.util.List;

public interface IRepoApi {

    Result<List<RepoItemResponse>> list();

    Result<Void> add(RepoActionRequest request);

    Result<Void> remove(RepoActionRequest request);

    Result<Void> fork(RepoForkRequest request);
}
