package com.dasi.domain.ai.service.execute.loop;

import com.dasi.domain.ai.model.entity.ExecuteRequestEntity;
import com.dasi.domain.ai.model.entity.ExecuteResponseEntity;
import com.dasi.domain.ai.service.execute.ExecuteContext;
import com.dasi.domain.ai.service.execute.IExecuteStrategy;
import com.dasi.domain.ai.service.execute.loop.node.LoopRootNode;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Slf4j
@Service
public class ExecuteLoopStrategy implements IExecuteStrategy {

    @Resource
    private LoopRootNode loopRootNode;

    @Override
    public void execute(ExecuteRequestEntity executeRequestEntity, SseEmitter sseEmitter) throws Exception {

        ExecuteContext executeContext = new ExecuteContext();
        executeContext.setValue("sseEmitter", sseEmitter);

        log.info("【任务执行】执行 LoopStrategy");
        loopRootNode.apply(executeRequestEntity, executeContext);

        try {
            ExecuteResponseEntity completeResult = ExecuteResponseEntity.createCompleteResponse("执行完成", executeRequestEntity.getSessionId());
            sseEmitter.send(SseEmitter.event()
                    .name("complete")
                    .data(completeResult));
        } catch (Exception e) {
            log.error("【任务执行】Loop 策略执行失败", e);
        }

    }

    @Override
    public String getType() {
        return "loop";
    }

}
