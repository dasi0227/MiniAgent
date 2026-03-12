package com.dasi.domain.ai.service.execute.step;

import com.dasi.domain.ai.model.entity.ExecuteRequestEntity;
import com.dasi.domain.ai.model.entity.ExecuteResponseEntity;
import com.dasi.domain.ai.service.execute.ExecuteContext;
import com.dasi.domain.ai.service.execute.IExecuteStrategy;
import com.dasi.domain.ai.service.execute.step.node.StepRootNode;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Slf4j
@Service
public class ExecuteStepStrategy implements IExecuteStrategy {

    @Resource
    private StepRootNode stepRootNode;

    @Override
    public void execute(ExecuteRequestEntity executeRequestEntity, SseEmitter sseEmitter) throws Exception {

        ExecuteContext executeContext = new ExecuteContext();
        executeContext.setValue("sseEmitter", sseEmitter);

        log.info("【任务执行】策略=StepStrategy");
        stepRootNode.apply(executeRequestEntity, executeContext);

        try {
            ExecuteResponseEntity completeResult = ExecuteResponseEntity.createCompleteResponse("执行完成", executeRequestEntity.getSessionId());
            sseEmitter.send(SseEmitter.event()
                    .name("complete")
                    .data(completeResult));
        } catch (Exception e) {
            log.error("【任务执行】Step 策略执行失败", e);
        }
    }

    @Override
    public String getType() {
        return "step";
    }

}
