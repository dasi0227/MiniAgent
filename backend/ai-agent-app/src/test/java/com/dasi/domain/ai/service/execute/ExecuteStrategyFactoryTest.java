package com.dasi.domain.ai.service.execute;

import com.dasi.domain.ai.repository.IAiRepository;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ExecuteStrategyFactoryTest {

    @Test
    void shouldResolveReactStrategyByAgentId() {
        IExecuteStrategy reactStrategy = mock(IExecuteStrategy.class);
        IExecuteStrategy loopStrategy = mock(IExecuteStrategy.class);
        when(reactStrategy.getType()).thenReturn("react");
        when(loopStrategy.getType()).thenReturn("loop");

        ExecuteStrategyFactory executeStrategyFactory = new ExecuteStrategyFactory(Map.of(
                "reactStrategy", reactStrategy,
                "loopStrategy", loopStrategy
        ));

        IAiRepository aiRepository = mock(IAiRepository.class);
        when(aiRepository.queryExecuteTypeByAgentId("agent_react")).thenReturn("react");
        ReflectionTestUtils.setField(executeStrategyFactory, "aiRepository", aiRepository);

        assertSame(reactStrategy, executeStrategyFactory.getStrategyByAgentId("agent_react"));
    }

}
