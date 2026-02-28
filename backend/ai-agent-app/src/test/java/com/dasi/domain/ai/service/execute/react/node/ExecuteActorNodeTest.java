package com.dasi.domain.ai.service.execute.react.node;

import com.dasi.domain.ai.model.entity.ExecuteRequestEntity;
import com.dasi.domain.ai.service.execute.ExecuteContext;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ExecuteActorNodeTest {

    @Test
    void shouldRouteBackToObserverWhenRoundWithinLimit() throws Exception {
        ExecuteActorNode executeActorNode = new ExecuteActorNode();

        ApplicationContext applicationContext = mock(ApplicationContext.class);
        ExecuteObserverNode executeObserverNode = mock(ExecuteObserverNode.class);
        ExecuteEvaluatorNode executeEvaluatorNode = mock(ExecuteEvaluatorNode.class);
        when(applicationContext.getBean("observerNode")).thenReturn(executeObserverNode);
        when(applicationContext.getBean("evaluatorNode")).thenReturn(executeEvaluatorNode);
        ReflectionTestUtils.setField(executeActorNode, "applicationContext", applicationContext);

        ExecuteContext executeContext = new ExecuteContext();
        executeContext.setRound(2);
        executeContext.setMaxRound(3);

        assertSame(executeObserverNode, executeActorNode.get(new ExecuteRequestEntity(), executeContext));
    }

    @Test
    void shouldRouteToEvaluatorWhenRoundExceededLimit() throws Exception {
        ExecuteActorNode executeActorNode = new ExecuteActorNode();

        ApplicationContext applicationContext = mock(ApplicationContext.class);
        ExecuteObserverNode executeObserverNode = mock(ExecuteObserverNode.class);
        ExecuteEvaluatorNode executeEvaluatorNode = mock(ExecuteEvaluatorNode.class);
        when(applicationContext.getBean("observerNode")).thenReturn(executeObserverNode);
        when(applicationContext.getBean("evaluatorNode")).thenReturn(executeEvaluatorNode);
        ReflectionTestUtils.setField(executeActorNode, "applicationContext", applicationContext);

        ExecuteContext executeContext = new ExecuteContext();
        executeContext.setRound(4);
        executeContext.setMaxRound(3);

        assertSame(executeEvaluatorNode, executeActorNode.get(new ExecuteRequestEntity(), executeContext));
    }

}
