package com.dasi.domain.ai.service.armory.strategy;

import com.dasi.domain.ai.model.entity.ArmoryRequestEntity;
import com.dasi.domain.ai.model.vo.*;
import com.dasi.domain.ai.repository.IAiRepository;
import com.dasi.domain.ai.service.armory.ArmoryContext;
import com.dasi.domain.ai.service.armory.IArmoryStrategy;
import com.dasi.domain.util.jwt.AuthContext;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.function.Supplier;

import static com.dasi.domain.ai.model.enumeration.AiArmoryType.ARMORY_CHAT;
import static com.dasi.domain.ai.model.enumeration.AiType.*;

@Slf4j
@Service("armoryChatStrategy")
public class ArmoryChatStrategy implements IArmoryStrategy {

    @Resource
    private IAiRepository aiRepository;

    @Resource
    private ThreadPoolExecutor threadPoolExecutor;

    @Resource
    private AuthContext authContext;

    @Override
    public void armory(ArmoryRequestEntity armoryRequestEntity, ArmoryContext armoryContext) {

        Set<String> clientIdSet = armoryRequestEntity.getArmoryIdSet();
        if (clientIdSet == null || clientIdSet.isEmpty()) {
            log.warn("【装配数据】Agent 装配：clientIdSet 为空");
            throw new IllegalStateException("装配数据时 clientIdSet 为空");
        }

        AuthContext.UserInfo userInfo = authContext.getUser();
        CompletableFuture<Set<AiApiVO>> aiApiSetFuture = supplyAsyncWithAuth(
                () -> aiRepository.queryAiApiVOSetByClientIdSet(clientIdSet), userInfo);

        CompletableFuture<Set<AiModelVO>> aiModelSetFuture = supplyAsyncWithAuth(
                () -> aiRepository.queryAiModelVOSetByClientIdSet(clientIdSet), userInfo);

        CompletableFuture<Set<AiMcpVO>> aiMcpSetFuture = supplyAsyncWithAuth(
                () -> aiRepository.queryAiMcpVOSetByClientIdSet(clientIdSet), userInfo);

        CompletableFuture<Map<String, AiPromptVO>> aiPromptMapFuture = supplyAsyncWithAuth(
                () -> aiRepository.queryAiPromptVOMapByClientIdSet(clientIdSet), userInfo);

        CompletableFuture<Set<AiAdvisorVO>> aiAdvisorSetFuture = supplyAsyncWithAuth(
                () -> aiRepository.queryAiAdvisorVOSetByClientIdSet(clientIdSet), userInfo);

        CompletableFuture<Set<AiClientVO>> aiClientSetFuture = supplyAsyncWithAuth(
                () -> aiRepository.queryAiClientVOSetByClientIdSet(clientIdSet), userInfo);

        CompletableFuture.allOf(
                aiApiSetFuture,
                aiModelSetFuture,
                aiMcpSetFuture,
                aiPromptMapFuture,
                aiAdvisorSetFuture,
                aiClientSetFuture
        ).join();

        Set<AiApiVO> aiApiSet = aiApiSetFuture.join();
        Set<AiModelVO> aiModelSet = aiModelSetFuture.join();
        Set<AiMcpVO> aiMcpSet = aiMcpSetFuture.join();
        Map<String, AiPromptVO> aiPrompMap = aiPromptMapFuture.join();
        Set<AiAdvisorVO> aiAdvisorSet = aiAdvisorSetFuture.join();
        Set<AiClientVO> aiClientSet = aiClientSetFuture.join();

        armoryContext.setValue(API.getType(), aiApiSet);
        armoryContext.setValue(MODEL.getType(), aiModelSet);
        armoryContext.setValue(MCP.getType(), aiMcpSet);
        armoryContext.setValue(PROMPT.getType(), aiPrompMap);
        armoryContext.setValue(ADVISOR.getType(), aiAdvisorSet);
        armoryContext.setValue(CLIENT.getType(), aiClientSet);

        log.info("【加载数据】ai_api_ids={}, size={}",
                aiApiSet.stream().map(AiApiVO::getApiId).toList(),
                aiApiSet.size());

        log.info("【加载数据】ai_model_ids={}, size={}",
                aiModelSet.stream().map(AiModelVO::getModelId).toList(),
                aiModelSet.size());

        log.info("【加载数据】ai_mcp_ids={}, size={}",
                aiMcpSet.stream().map(AiMcpVO::getMcpId).toList(),
                aiMcpSet.size());

        log.info("【加载数据】ai_prompt_ids={}, size={}",
                aiPrompMap.values().stream().map(AiPromptVO::getPromptId).toList(),
                aiPrompMap.size());

        log.info("【加载数据】ai_advisor_ids={}, size={}",
                aiAdvisorSet.stream().map(AiAdvisorVO::getAdvisorId).toList(),
                aiAdvisorSet.size());

        log.info("【加载数据】ai_client_ids={}, size={}",
                aiClientSet.stream().map(AiClientVO::getClientId).toList(),
                aiClientSet.size());

    }

    @Override
    public String getType() {
        return ARMORY_CHAT.getType();
    }

    private <T> CompletableFuture<T> supplyAsyncWithAuth(Supplier<T> supplier, AuthContext.UserInfo userInfo) {
        return CompletableFuture.supplyAsync(() -> {
            if (userInfo != null) {
                authContext.set(userInfo);
            }
            try {
                return supplier.get();
            } finally {
                authContext.clear();
            }
        }, threadPoolExecutor);
    }

}
