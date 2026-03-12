package com.dasi.domain.ai.service.execute.step.node;

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

import static com.dasi.domain.ai.model.enumeration.AiClientRole.REPLIER;
import static com.dasi.domain.ai.model.enumeration.AiSectionType.REPLIER_OVERVIEW;
import static com.dasi.domain.ai.model.enumeration.AiType.CLIENT;
import static com.dasi.types.constant.ExceptionMessage.EXECUTE_REPLIER_RESULT_EMPTY;

@Slf4j
@Service(value = "replierNode")
public class StepReplierNode extends AbstractExecuteNode {

    @Override
    protected String doApply(ExecuteRequestEntity executeRequestEntity, ExecuteContext executeContext) throws Exception {

        String replierJson;
        JSONObject replierObject;

        String executionHistory = executeContext.getExecutionHistory().toString();
        if (executionHistory.isEmpty()) {
            executionHistory = "[暂无记录]";
        }

        try {

            // 获取客户端
            AiFlowVO aiFlowVO = executeContext.getAiFlowVOMap().get(REPLIER.getRole());
            String clientBeanName = CLIENT.getBeanName(aiFlowVO.getClientId());
            ChatClient replierClient = getBean(clientBeanName);

            // 获取提示词
            String userPrompt = aiFlowVO.getUserPrompt();
            String replierPrompt = userPrompt.formatted(
                    executeContext.getUserMessage(),
                    executionHistory
            );

            // 获取客户端结果
            String replierResponse = replierClient
                    .prompt(replierPrompt)
                    .call()
                    .content();

            // 解析客户端结果
            replierJson = extractJson(replierResponse, "{}");
            replierObject = parseJsonObject(replierJson);
            if (replierObject == null) {
                throw new MissingException(EXECUTE_REPLIER_RESULT_EMPTY);
            }

        } catch (Exception e) {
            log.error("【执行节点】StepReplierNode", e);
            replierObject = buildExceptionObject(REPLIER.getExceptionType(), e.getMessage());
            replierJson = replierObject.toJSONString();
        }

        // 发送客户端结果
        parseReplierResponse(executeContext, replierObject, executeRequestEntity.getSessionId());

        // 保存客户端结果
        executeContext.setValue(REPLIER.getContextKey(), replierJson);

        return router(executeRequestEntity, executeContext);
    }

    @Override
    public StrategyHandler<ExecuteRequestEntity, ExecuteContext, String> get(ExecuteRequestEntity executeRequestEntity, ExecuteContext executeContext) throws Exception {
        return defaultStrategyHandler;
    }

    private void parseReplierResponse(ExecuteContext executeContext, JSONObject replierObject, String sessionId) {
        if (replierObject == null) {
            return;
        }
        sendReplierResponse(executeContext, REPLIER.getExceptionType(), replierObject.getString(REPLIER.getExceptionType()), sessionId);
        sendReplierResponse(executeContext, REPLIER_OVERVIEW.getType(), replierObject.getString(REPLIER_OVERVIEW.getType()), sessionId);
    }

    private void sendReplierResponse(ExecuteContext executeContext, String sectionType, String sectionContent, String sessionId) {
        if (!sectionType.isEmpty() && sectionContent != null && !sectionContent.isEmpty()) {
            ExecuteResponseEntity executeResponseEntity = ExecuteResponseEntity.createReplierResponse(
                    sectionType,
                    sectionContent,
                    sessionId
            );

            sendSseMessage(executeContext, executeResponseEntity);
        }
    }

}
