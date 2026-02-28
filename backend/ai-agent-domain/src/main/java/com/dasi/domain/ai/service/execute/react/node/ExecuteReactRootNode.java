package com.dasi.domain.ai.service.execute.react.node;

import cn.bugstack.wrench.design.framework.tree.StrategyHandler;
import com.dasi.domain.ai.model.entity.ExecuteRequestEntity;
import com.dasi.domain.ai.model.vo.AiFlowVO;
import com.dasi.domain.ai.service.execute.AbstractExecuteNode;
import com.dasi.domain.ai.service.execute.ExecuteContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

import static com.dasi.domain.ai.model.enumeration.AiClientRole.OBSERVER;

@Slf4j
@Service
public class ExecuteReactRootNode extends AbstractExecuteNode {

    @Override
    protected String doApply(ExecuteRequestEntity executeRequestEntity, ExecuteContext executeContext) throws Exception {

        Map<String, AiFlowVO> aiFlowVOMap = aiRepository.queryAiFlowVOMapByAgentId(executeRequestEntity.getAgentId());

        executeContext.setAiFlowVOMap(aiFlowVOMap);
        executeContext.setRound(1);
        executeContext.setMaxRound(executeRequestEntity.getMaxRound());
        executeContext.setCompleted(false);
        executeContext.setExecutionHistory(new StringBuilder());
        executeContext.setUserMessage(executeRequestEntity.getUserMessage());
        executeContext.setCurrentTask("请先观察当前任务状态，并判断是否需要继续执行下一小步。");

        log.info("【执行节点】ExecuteReactRootNode：userMessage={}", executeRequestEntity.getUserMessage());
        return router(executeRequestEntity, executeContext);
    }

    @Override
    public StrategyHandler<ExecuteRequestEntity, ExecuteContext, String> get(ExecuteRequestEntity executeRequestEntity, ExecuteContext executeContext) throws Exception {
        return getBean(OBSERVER.getNodeName());
    }

}
