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
import static com.dasi.domain.ai.model.enumeration.AiSectionType.EVALUATOR_OVERVIEW;
import static com.dasi.domain.ai.model.enumeration.AiType.CLIENT;

@Slf4j
@Service(value = "evaluatorNode")
public class ReactEvaluatorNode extends AbstractExecuteNode {

    @Override
    protected String doApply(ExecuteRequestEntity executeRequestEntity, ExecuteContext executeContext) throws Exception {

        String evaluatorJson;
        JSONObject evaluatorObject;

        String executionHistory = executeContext.getExecutionHistory().toString();
        if (executionHistory.isEmpty()) {
            executionHistory = "[暂无记录]";
        }

        try {
            AiFlowVO aiFlowVO = executeContext.getAiFlowVOMap().get(EVALUATOR.getRole());
            String clientBeanName = CLIENT.getBeanName(aiFlowVO.getClientId());
            ChatClient evaluatorClient = getBean(clientBeanName);

            String userPrompt = aiFlowVO.getUserPrompt();
            String evaluatorPrompt = userPrompt.formatted(
                    executeContext.getUserMessage(),
                    executionHistory
            );

            String evaluatorResponse = evaluatorClient
                    .prompt(evaluatorPrompt)
                    .call()
                    .content();

            evaluatorJson = extractJson(evaluatorResponse, "{}");
            evaluatorObject = parseJsonObject(evaluatorJson);
            if (evaluatorObject == null) {
                throw new IllegalStateException("Evaluator 结果解析为空");
            }
        } catch (Exception e) {
            log.error("【执行节点】ReactEvaluatorNode：error={}", e.getMessage(), e);
            evaluatorObject = buildExceptionObject(EVALUATOR.getExceptionType(), e.getMessage());
            evaluatorJson = evaluatorObject.toJSONString();
        }

        parseEvaluatorResponse(executeContext, evaluatorObject, executeRequestEntity.getSessionId());
        executeContext.setValue(EVALUATOR.getContextKey(), evaluatorJson);

        return router(executeRequestEntity, executeContext);
    }

    @Override
    public StrategyHandler<ExecuteRequestEntity, ExecuteContext, String> get(ExecuteRequestEntity executeRequestEntity, ExecuteContext executeContext) throws Exception {
        return defaultStrategyHandler;
    }

    private void parseEvaluatorResponse(ExecuteContext executeContext, JSONObject evaluatorObject, String sessionId) {
        if (evaluatorObject == null) {
            return;
        }
        sendEvaluatorResponse(executeContext, EVALUATOR.getExceptionType(), evaluatorObject.getString(EVALUATOR.getExceptionType()), sessionId);
        sendEvaluatorResponse(executeContext, EVALUATOR_OVERVIEW.getType(), evaluatorObject.getString(EVALUATOR_OVERVIEW.getType()), sessionId);
    }

    private void sendEvaluatorResponse(ExecuteContext executeContext, String sectionType, String sectionContent, String sessionId) {
        if (!sectionType.isEmpty() && sectionContent != null && !sectionContent.isEmpty()) {
            ExecuteResponseEntity executeResponseEntity = ExecuteResponseEntity.createEvaluatorResponse(
                    sectionType,
                    sectionContent,
                    executeContext.getPace(),
                    sessionId
            );

            sendSseMessage(executeContext, executeResponseEntity);
        }
    }

}
