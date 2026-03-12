package com.dasi.domain.ai.service.execute.react.node;

import cn.bugstack.wrench.design.framework.tree.StrategyHandler;
import com.alibaba.fastjson2.JSONObject;
import com.dasi.domain.ai.model.entity.ExecuteRequestEntity;
import com.dasi.domain.ai.model.entity.ExecuteResponseEntity;
import com.dasi.domain.ai.model.vo.AiFlowVO;
import com.dasi.domain.ai.service.execute.AbstractExecuteNode;
import com.dasi.domain.ai.service.execute.ExecuteContext;
import com.dasi.types.exception.MissingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import static com.dasi.domain.ai.model.enumeration.AiClientRole.ACTOR;
import static com.dasi.domain.ai.model.enumeration.AiClientRole.OBSERVER;
import static com.dasi.domain.ai.model.enumeration.AiClientRole.REASONER;
import static com.dasi.domain.ai.model.enumeration.AiSectionType.REASONER_ACCEPTANCE;
import static com.dasi.domain.ai.model.enumeration.AiSectionType.REASONER_ACTION;
import static com.dasi.domain.ai.model.enumeration.AiSectionType.REASONER_TARGET;
import static com.dasi.domain.ai.model.enumeration.AiType.CLIENT;
import static com.dasi.types.constant.ChatConstant.CHAT_MEMORY_CONVERSATION_ID_KEY;
import static com.dasi.types.constant.ChatConstant.CHAT_MEMORY_RETRIEVE_SIZE_KEY;
import static com.dasi.types.constant.ChatConstant.CHAT_MEMORY_RETRIEVE_SIZE_WORK;
import static com.dasi.types.constant.ExceptionMessage.EXECUTE_REASONER_RESULT_EMPTY;

@Slf4j
@Service(value = "reasonerNode")
public class ReactReasonerNode extends AbstractExecuteNode {

    @Override
    protected String doApply(ExecuteRequestEntity executeRequestEntity, ExecuteContext executeContext) throws Exception {

        String reasonerJson;
        JSONObject reasonerObject;

        String observerResponse = executeContext.getValue(OBSERVER.getContextKey());
        if (observerResponse == null || observerResponse.isBlank()) {
            observerResponse = "[任务观察专家异常，请依据用户原始需求制定下一小步]";
        }

        String executionHistory = executeContext.getExecutionHistory().toString();
        if (executionHistory.isEmpty()) {
            executionHistory = "[暂无记录]";
        }

        try {
            AiFlowVO aiFlowVO = executeContext.getAiFlowVOMap().get(REASONER.getRole());
            String clientBeanName = CLIENT.getBeanName(aiFlowVO.getClientId());
            ChatClient reasonerClient = getBean(clientBeanName);

            String userPrompt = aiFlowVO.getUserPrompt();
            String reasonerPrompt = userPrompt.formatted(
                    executeContext.getUserMessage(),
                    observerResponse,
                    executionHistory
            );

            String reasonerResponse = reasonerClient
                    .prompt(reasonerPrompt)
                    .advisors(a -> a
                            .param(CHAT_MEMORY_CONVERSATION_ID_KEY, executeRequestEntity.getSessionId())
                            .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, CHAT_MEMORY_RETRIEVE_SIZE_WORK))
                    .call()
                    .content();

            reasonerJson = extractJson(reasonerResponse, "{}");
            reasonerObject = parseJsonObject(reasonerJson);
            if (reasonerObject == null) {
                throw new MissingException(EXECUTE_REASONER_RESULT_EMPTY);
            }
        } catch (Exception e) {
            log.error("【执行节点】ReactReasonerNode", e);
            reasonerObject = buildExceptionObject(REASONER.getExceptionType(), e.getMessage());
            reasonerJson = reasonerObject.toJSONString();
        }

        parseReasonerResponse(executeContext, reasonerObject, executeRequestEntity.getSessionId());
        executeContext.setValue(REASONER.getContextKey(), reasonerJson);

        String currentTask = reasonerObject.getString(REASONER_ACTION.getType());
        if (currentTask == null || currentTask.isBlank()) {
            currentTask = reasonerJson;
        }
        executeContext.setCurrentTask(currentTask);

        return router(executeRequestEntity, executeContext);
    }

    @Override
    public StrategyHandler<ExecuteRequestEntity, ExecuteContext, String> get(ExecuteRequestEntity executeRequestEntity, ExecuteContext executeContext) throws Exception {
        return getBean(ACTOR.getNodeName());
    }

    private void parseReasonerResponse(ExecuteContext executeContext, JSONObject reasonerObject, String sessionId) {
        if (reasonerObject == null) {
            return;
        }
        sendReasonerResponse(executeContext, REASONER.getExceptionType(), reasonerObject.getString(REASONER.getExceptionType()), sessionId);
        sendReasonerResponse(executeContext, REASONER_TARGET.getType(), reasonerObject.getString(REASONER_TARGET.getType()), sessionId);
        sendReasonerResponse(executeContext, REASONER_ACTION.getType(), reasonerObject.getString(REASONER_ACTION.getType()), sessionId);
        sendReasonerResponse(executeContext, REASONER_ACCEPTANCE.getType(), reasonerObject.getString(REASONER_ACCEPTANCE.getType()), sessionId);
    }

    private void sendReasonerResponse(ExecuteContext executeContext, String sectionType, String sectionContent, String sessionId) {
        if (!sectionType.isEmpty() && sectionContent != null && !sectionContent.isEmpty()) {
            ExecuteResponseEntity executeResponseEntity = ExecuteResponseEntity.createReasonerResponse(
                    sectionType,
                    sectionContent,
                    executeContext.getPace(),
                    sessionId
            );

            sendSseMessage(executeContext, executeResponseEntity);
        }
    }

}
