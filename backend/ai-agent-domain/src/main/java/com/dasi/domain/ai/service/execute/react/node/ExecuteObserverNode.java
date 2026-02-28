package com.dasi.domain.ai.service.execute.react.node;

import cn.bugstack.wrench.design.framework.tree.StrategyHandler;
import com.alibaba.fastjson2.JSONObject;
import com.dasi.domain.ai.model.entity.ExecuteRequestEntity;
import com.dasi.domain.ai.model.entity.ExecuteResponseEntity;
import com.dasi.domain.ai.model.vo.AiFlowVO;
import com.dasi.domain.ai.service.execute.AbstractExecuteNode;
import com.dasi.domain.ai.service.execute.ExecuteContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import static com.dasi.domain.ai.model.enumeration.AiClientRole.EVALUATOR;
import static com.dasi.domain.ai.model.enumeration.AiClientRole.OBSERVER;
import static com.dasi.domain.ai.model.enumeration.AiClientRole.REASONER;
import static com.dasi.domain.ai.model.enumeration.AiSectionType.OBSERVER_DEMAND;
import static com.dasi.domain.ai.model.enumeration.AiSectionType.OBSERVER_HISTORY;
import static com.dasi.domain.ai.model.enumeration.AiSectionType.OBSERVER_JUDGEMENT;
import static com.dasi.domain.ai.model.enumeration.AiSectionType.OBSERVER_STATUS;
import static com.dasi.domain.ai.model.enumeration.AiType.CLIENT;
import static com.dasi.types.constant.ChatConstant.CHAT_MEMORY_CONVERSATION_ID_KEY;
import static com.dasi.types.constant.ChatConstant.CHAT_MEMORY_RETRIEVE_SIZE_KEY;
import static com.dasi.types.constant.ChatConstant.CHAT_MEMORY_RETRIEVE_SIZE_WORK;

@Slf4j
@Service(value = "observerNode")
public class ExecuteObserverNode extends AbstractExecuteNode {

    @Override
    protected String doApply(ExecuteRequestEntity executeRequestEntity, ExecuteContext executeContext) throws Exception {

        String observerJson;
        JSONObject observerObject;

        String executionHistory = executeContext.getExecutionHistory().toString();
        if (executionHistory.isEmpty()) {
            executionHistory = "[暂无记录]";
        }

        String currentTask = executeContext.getCurrentTask();
        if (currentTask == null || currentTask.isBlank()) {
            currentTask = "[请结合用户原始需求观察当前状态并判断是否继续执行]";
        }

        try {
            AiFlowVO aiFlowVO = executeContext.getAiFlowVOMap().get(OBSERVER.getRole());
            String clientBeanName = CLIENT.getBeanName(aiFlowVO.getClientId());
            ChatClient observerClient = getBean(clientBeanName);

            String userPrompt = aiFlowVO.getUserPrompt();
            String observerPrompt = userPrompt.formatted(
                    executeContext.getRound(),
                    executeContext.getMaxRound(),
                    executeContext.getUserMessage(),
                    currentTask,
                    executionHistory
            );

            String observerResponse = observerClient
                    .prompt(observerPrompt)
                    .advisors(a -> a
                            .param(CHAT_MEMORY_CONVERSATION_ID_KEY, executeRequestEntity.getSessionId())
                            .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, CHAT_MEMORY_RETRIEVE_SIZE_WORK))
                    .call()
                    .content();

            observerJson = extractJson(observerResponse, "{}");
            observerObject = parseJsonObject(observerJson);
            if (observerObject == null) {
                throw new IllegalStateException("Observer 结果解析为空");
            }
        } catch (Exception e) {
            log.error("【执行节点】ExecuteObserverNode：error={}", e.getMessage(), e);
            observerObject = buildExceptionObject(OBSERVER.getExceptionType(), e.getMessage());
            observerJson = observerObject.toJSONString();
        }

        parseObserverResponse(executeContext, observerObject, executeRequestEntity.getSessionId());
        executeContext.setValue(OBSERVER.getContextKey(), observerJson);

        String observerStatus = observerObject.getString(OBSERVER_STATUS.getType());
        if ("COMPLETED".equalsIgnoreCase(observerStatus)
                || "DONE".equalsIgnoreCase(observerStatus)
                || "PASS".equalsIgnoreCase(observerStatus)) {
            executeContext.setCompleted(true);
        } else {
            executeContext.setCompleted(false);
        }

        return router(executeRequestEntity, executeContext);
    }

    @Override
    public StrategyHandler<ExecuteRequestEntity, ExecuteContext, String> get(ExecuteRequestEntity executeRequestEntity, ExecuteContext executeContext) throws Exception {
        if (Boolean.TRUE.equals(executeContext.getCompleted())) {
            log.info("【执行节点】ExecuteObserverNode：任务观察判定完成");
            return getBean(EVALUATOR.getNodeName());
        }
        return getBean(REASONER.getNodeName());
    }

    private void parseObserverResponse(ExecuteContext executeContext, JSONObject observerObject, String sessionId) {
        if (observerObject == null) {
            return;
        }
        sendObserverResponse(executeContext, OBSERVER.getExceptionType(), observerObject.getString(OBSERVER.getExceptionType()), sessionId);
        sendObserverResponse(executeContext, OBSERVER_DEMAND.getType(), observerObject.getString(OBSERVER_DEMAND.getType()), sessionId);
        sendObserverResponse(executeContext, OBSERVER_HISTORY.getType(), observerObject.getString(OBSERVER_HISTORY.getType()), sessionId);
        sendObserverResponse(executeContext, OBSERVER_JUDGEMENT.getType(), observerObject.getString(OBSERVER_JUDGEMENT.getType()), sessionId);
        sendObserverResponse(executeContext, OBSERVER_STATUS.getType(), observerObject.getString(OBSERVER_STATUS.getType()), sessionId);
    }

    private void sendObserverResponse(ExecuteContext executeContext, String sectionType, String sectionContent, String sessionId) {
        if (!sectionType.isEmpty() && sectionContent != null && !sectionContent.isEmpty()) {
            ExecuteResponseEntity executeResponseEntity = ExecuteResponseEntity.createObserverResponse(
                    sectionType,
                    sectionContent,
                    executeContext.getRound(),
                    sessionId
            );

            sendSseMessage(executeContext, executeResponseEntity);
        }
    }

}
