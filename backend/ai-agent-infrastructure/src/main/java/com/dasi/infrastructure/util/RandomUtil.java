package com.dasi.infrastructure.util;

import com.dasi.domain.util.jwt.UserContext;
import com.dasi.domain.util.random.IRandomUtil;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class RandomUtil implements IRandomUtil {

    @Resource
    private UserContext userContext;

    private String userRandom() {
        return userContext.getUserId() + "_" + String.valueOf(System.currentTimeMillis()).substring(0, 4) + RandomStringUtils.randomAlphanumeric(4);
    }

    @Override
    public String randomApiId() {
        return "api_" + userRandom();
    }

    @Override
    public String randomModelId() {
        return "model_" + userRandom();
    }

    @Override
    public String randomMcpId() {
        return "mcp_" + userRandom();
    }

    @Override
    public String randomAgentId() {
        return "agent_" + userRandom();
    }

    @Override
    public String randomClientId() {
        return "client_" + userRandom();
    }

    @Override
    public String randomPromptId() {
        return "prompt_" + userRandom();
    }

    @Override
    public String randomRepoId() {
        return "repo_" + userRandom();
    }

    @Override
    public String randomTemplateId() {
        return "template_" + userRandom();
    }

    @Override
    public String randomPlazaId() {
        return "plaza_" + userRandom();
    }

    @Override
    public String randomCommentId() {
        return "comment_" + userRandom();
    }

    @Override
    public String randomTaskId() {
        return "task_" + userRandom();
    }

    @Override
    public String uuid() {
        return UUID.randomUUID().toString().replace("-", "");
    }

}
