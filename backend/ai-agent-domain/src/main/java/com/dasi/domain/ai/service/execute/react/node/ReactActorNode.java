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

import static com.dasi.domain.ai.model.enumeration.AiClientRole.ACTOR;
import static com.dasi.domain.ai.model.enumeration.AiClientRole.EVALUATOR;
import static com.dasi.domain.ai.model.enumeration.AiClientRole.OBSERVER;
import static com.dasi.domain.ai.model.enumeration.AiClientRole.REASONER;
import static com.dasi.domain.ai.model.enumeration.AiSectionType.ACTOR_PROCESS;
import static com.dasi.domain.ai.model.enumeration.AiSectionType.ACTOR_RESULT;
import static com.dasi.domain.ai.model.enumeration.AiSectionType.ACTOR_TARGET;
import static com.dasi.domain.ai.model.enumeration.AiType.CLIENT;
import static com.dasi.types.constant.ChatConstant.CHAT_MEMORY_CONVERSATION_ID_KEY;
import static com.dasi.types.constant.ChatConstant.CHAT_MEMORY_RETRIEVE_SIZE_KEY;
import static com.dasi.types.constant.ChatConstant.CHAT_MEMORY_RETRIEVE_SIZE_WORK;

@Slf4j
@Service(value = "actorNode")
public class ReactActorNode extends AbstractExecuteNode {

    @Override
    protected String doApply(ExecuteRequestEntity executeRequestEntity, ExecuteContext executeContext) throws Exception {

        String actorJson;
        JSONObject actorObject;

        String observerResponse = executeContext.getValue(OBSERVER.getContextKey());
        String reasonerResponse = executeContext.getValue(REASONER.getContextKey());

        if (observerResponse == null || observerResponse.isBlank()) {
            observerResponse = "[任务观察专家异常，请依据用户原始需求和本轮目标执行任务]";
        }
        if (reasonerResponse == null || reasonerResponse.isBlank()) {
            reasonerResponse = "[任务推理专家异常，请自行决定当前一小步的执行方式]";
        }

        try {
            AiFlowVO aiFlowVO = executeContext.getAiFlowVOMap().get(ACTOR.getRole());
            String clientBeanName = CLIENT.getBeanName(aiFlowVO.getClientId());
            ChatClient actorClient = getBean(clientBeanName);

            String userPrompt = aiFlowVO.getUserPrompt();
            String actorPrompt = userPrompt.formatted(
                    executeContext.getUserMessage(),
                    observerResponse,
                    reasonerResponse
            );

            String actorResponse = actorClient
                    .prompt(actorPrompt)
                    .advisors(a -> a
                            .param(CHAT_MEMORY_CONVERSATION_ID_KEY, executeRequestEntity.getSessionId())
                            .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, CHAT_MEMORY_RETRIEVE_SIZE_WORK))
                    .call()
                    .content();

            actorJson = extractJson(actorResponse, "{}");
            actorObject = parseJsonObject(actorJson);
            if (actorObject == null) {
                throw new IllegalStateException("Actor 结果解析为空");
            }
        } catch (Exception e) {
            log.error("【执行节点】ReactActorNode：error={}", e.getMessage(), e);
            actorObject = buildExceptionObject(ACTOR.getExceptionType(), e.getMessage());
            actorJson = actorObject.toJSONString();
        }

        parseActorResponse(executeContext, actorObject, executeRequestEntity.getSessionId());
        executeContext.setValue(ACTOR.getContextKey(), actorJson);

        String executionHistory = String.format("""
                        === 第 %d 轮执行记录 ===
                        【任务观察专家】
                        %s
                        【任务推理专家】
                        %s
                        【任务行动专家】
                        %s
                        """,
                executeContext.getPace(),
                observerResponse,
                reasonerResponse,
                actorJson
        );
        executeContext.getExecutionHistory().append(executionHistory);

        String currentTask = actorObject.getString(ACTOR_RESULT.getType());
        if (currentTask == null || currentTask.isBlank()) {
            currentTask = actorJson;
        }
        executeContext.setCurrentTask(currentTask);
        executeContext.setPace(executeContext.getPace() + 1);

        return router(executeRequestEntity, executeContext);
    }

    @Override
    public StrategyHandler<ExecuteRequestEntity, ExecuteContext, String> get(ExecuteRequestEntity executeRequestEntity, ExecuteContext executeContext) throws Exception {
        if (executeContext.getPace() > executeContext.getMaxPace()) {
            log.info("【执行节点】ReactActorNode：任务已到达最大轮数");
            return getBean(EVALUATOR.getNodeName());
        }
        return getBean(OBSERVER.getNodeName());
    }

    private void parseActorResponse(ExecuteContext executeContext, JSONObject actorObject, String sessionId) {
        if (actorObject == null) {
            return;
        }
        sendActorResponse(executeContext, ACTOR.getExceptionType(), actorObject.getString(ACTOR.getExceptionType()), sessionId);
        sendActorResponse(executeContext, ACTOR_TARGET.getType(), actorObject.getString(ACTOR_TARGET.getType()), sessionId);
        sendActorResponse(executeContext, ACTOR_PROCESS.getType(), actorObject.getString(ACTOR_PROCESS.getType()), sessionId);
        sendActorResponse(executeContext, ACTOR_RESULT.getType(), actorObject.getString(ACTOR_RESULT.getType()), sessionId);
    }

    private void sendActorResponse(ExecuteContext executeContext, String sectionType, String sectionContent, String sessionId) {
        if (!sectionType.isEmpty() && sectionContent != null && !sectionContent.isEmpty()) {
            ExecuteResponseEntity executeResponseEntity = ExecuteResponseEntity.createActorResponse(
                    sectionType,
                    sectionContent,
                    executeContext.getPace(),
                    sessionId
            );

            sendSseMessage(executeContext, executeResponseEntity);
        }
    }

}
