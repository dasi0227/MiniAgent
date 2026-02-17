package com.dasi.domain.repo.service;

import com.dasi.domain.repo.repository.IRepoRepository;
import com.dasi.domain.util.jwt.AuthContext;
import com.dasi.types.dto.request.repo.RepoActionRequest;
import com.dasi.types.dto.request.repo.RepoForkRequest;
import com.dasi.types.dto.response.repo.RepoItemResponse;
import com.dasi.types.exception.AuthException;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RepoService implements IRepoService {

    @Resource
    private IRepoRepository repoRepository;

    @Resource
    private AuthContext authContext;

    @Override
    public List<RepoItemResponse> list() {
        return repoRepository.list(requiredUserId());
    }

    @Override
    public void add(RepoActionRequest request) {
        repoRepository.add(requiredUserId(), request.getAgentId());
    }

    @Override
    public void remove(RepoActionRequest request) {
        repoRepository.remove(requiredUserId(), request.getAgentId());
    }

    @Override
    public void fork(RepoForkRequest request) {
        repoRepository.fork(requiredUserId(), request.getPlazaId());
    }

    private Long requiredUserId() {
        Long userId = authContext.getId();
        if (userId == null) {
            throw new AuthException("未登录");
        }
        return userId;
    }
}
