package com.dasi.domain.ai.service.execute.react.node;

import com.dasi.domain.ai.model.entity.ExecuteRequestEntity;
import com.dasi.domain.ai.service.execute.ExecuteContext;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ExecuteObserverNodeTest {

    @Test
    void shouldRouteToEvaluatorWhenObserverMarkedCompleted() throws Exception {
        ExecuteObserverNode executeObserverNode = new ExecuteObserverNode();

        ApplicationContext applicationContext = mock(ApplicationContext.class);
        ExecuteEvaluatorNode executeEvaluatorNode = mock(ExecuteEvaluatorNode.class);
        ExecuteReasonerNode executeReasonerNode = mock(ExecuteReasonerNode.class);
        when(applicationContext.getBean("evaluatorNode")).thenReturn(executeEvaluatorNode);
        when(applicationContext.getBean("reasonerNode")).thenReturn(executeReasonerNode);
        ReflectionTestUtils.setField(executeObserverNode, "applicationContext", applicationContext);

        ExecuteContext executeContext = new ExecuteContext();
        executeContext.setCompleted(true);

        assertSame(executeEvaluatorNode, executeObserverNode.get(new ExecuteRequestEntity(), executeContext));
    }

    @Test
    void shouldRouteToReasonerWhenObserverMarkedUncompleted() throws Exception {
        ExecuteObserverNode executeObserverNode = new ExecuteObserverNode();

        ApplicationContext applicationContext = mock(ApplicationContext.class);
        ExecuteEvaluatorNode executeEvaluatorNode = mock(ExecuteEvaluatorNode.class);
        ExecuteReasonerNode executeReasonerNode = mock(ExecuteReasonerNode.class);
        when(applicationContext.getBean("evaluatorNode")).thenReturn(executeEvaluatorNode);
        when(applicationContext.getBean("reasonerNode")).thenReturn(executeReasonerNode);
        ReflectionTestUtils.setField(executeObserverNode, "applicationContext", applicationContext);

        ExecuteContext executeContext = new ExecuteContext();
        executeContext.setCompleted(false);

        assertSame(executeReasonerNode, executeObserverNode.get(new ExecuteRequestEntity(), executeContext));
    }

}
