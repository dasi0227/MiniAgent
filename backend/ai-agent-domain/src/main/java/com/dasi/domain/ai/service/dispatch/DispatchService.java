package com.dasi.domain.ai.service.dispatch;

import cn.bugstack.wrench.design.framework.tree.StrategyHandler;
import com.dasi.domain.ai.model.entity.ArmoryRequestEntity;
import com.dasi.domain.ai.model.entity.ExecuteRequestEntity;
import com.dasi.domain.ai.service.armory.ArmoryContext;
import com.dasi.domain.ai.service.armory.ArmoryStrategyFactory;
import com.dasi.domain.ai.service.armory.IArmoryStrategy;
import com.dasi.domain.ai.service.execute.ExecuteStrategyFactory;
import com.dasi.domain.ai.service.execute.IExecuteStrategy;
import com.dasi.domain.util.redis.IRedisUtil;
import com.dasi.types.exception.ArmoryException;
import com.dasi.types.exception.MissingException;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Set;
import java.util.concurrent.ThreadPoolExecutor;

import static com.dasi.types.constant.RedisConstant.ARMORY_PREFIX;
import static com.dasi.types.constant.ExceptionMessage.*;

@Slf4j
@Service
public class DispatchService implements IDispatchService {

    @Resource
    private ArmoryStrategyFactory armoryStrategyFactory;

    @Resource
    private ExecuteStrategyFactory executeStrategyFactory;

    @Resource
    private ThreadPoolExecutor threadPoolExecutor;

    @Resource
    private IRedisUtil redisUtil;

    @Override
    public void dispatchArmoryStrategy(String armoryType, Set<String> armoryIdSet) {

        IArmoryStrategy armoryStrategy = armoryStrategyFactory.getArmoryStrategyByType(armoryType);
        StrategyHandler<ArmoryRequestEntity, ArmoryContext, String> armoryRootNode = armoryStrategyFactory.getArmoryRootNode();

        if (armoryStrategy == null) {
            throw new MissingException(DISPATCH_ARMORY_STRATEGY_NOT_FOUND);
        }
        if (armoryRootNode == null) {
            throw new MissingException(DISPATCH_ARMORY_ENTRY_NOT_FOUND);
        }

        String armoryKey = ARMORY_PREFIX + armoryType;
        Set<String> cacheSet = redisUtil.getSet(armoryKey, String.class);

        if (cacheSet != null && !cacheSet.isEmpty()) {
            armoryIdSet.removeAll(cacheSet);
        }

        if (armoryIdSet.isEmpty()) {
            return;
        }

        ArmoryRequestEntity armoryRequestEntity = ArmoryRequestEntity.builder()
                .armoryType(armoryType)
                .armoryIdSet(armoryIdSet)
                .build();
        ArmoryContext armoryContext = new ArmoryContext();

        try {
            log.info("========================================================================================");
            armoryStrategy.armory(armoryRequestEntity, armoryContext);
            armoryRootNode.apply(armoryRequestEntity, armoryContext);
        } catch (Exception e) {
            log.error("【装配数据】装配失败", e);
            throw new ArmoryException(DISPATCH_ARMORY_FAIL);
        }

        redisUtil.addSet(armoryKey, armoryIdSet);
    }

    @Override
    public void dispatchExecuteStrategy(ExecuteRequestEntity executeRequestEntity, SseEmitter sseEmitter) {

        IExecuteStrategy executeStrategy = executeStrategyFactory.getStrategyByAgentId(executeRequestEntity.getAgentId());

        if (executeStrategy == null) {
            throw new MissingException(DISPATCH_EXECUTE_STRATEGY_NOT_FOUND);
        }

        threadPoolExecutor.execute(() -> {
            try {
                log.info("========================================================================================");
                executeStrategy.execute(executeRequestEntity, sseEmitter);
            } catch (Exception e) {
                try {
                    log.error("【任务执行】执行失败", e);
                    sseEmitter.send(SseEmitter.event()
                            .name("error")
                            .data("执行异常：" + e.getMessage()));
                } catch (Exception ex) {
                    log.error("【任务执行】发送 SSE 消息失败", e);
                }
            } finally {
                sseEmitter.complete();
            }
        });
    }

}
