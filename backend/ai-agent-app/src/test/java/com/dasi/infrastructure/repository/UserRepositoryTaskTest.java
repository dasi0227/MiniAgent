package com.dasi.infrastructure.repository;

import com.dasi.domain.user.model.dto.SettingTaskDTO;
import com.dasi.domain.user.model.vo.UserTaskVO;
import com.dasi.domain.util.jwt.UserContext;
import com.dasi.infrastructure.persistent.dao.IAiAgentDao;
import com.dasi.infrastructure.persistent.dao.IAiApiDao;
import com.dasi.infrastructure.persistent.dao.IAiMcpDao;
import com.dasi.infrastructure.persistent.dao.IAiModelDao;
import com.dasi.infrastructure.persistent.dao.IAiTaskDao;
import com.dasi.infrastructure.persistent.dao.IAiUserDao;
import com.dasi.infrastructure.persistent.po.AiAgent;
import com.dasi.infrastructure.persistent.po.AiTask;
import com.dasi.types.exception.MiniAgentException;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserRepositoryTaskTest {

    @Test
    void taskInsertShouldWriteCurrentUserOwnership() {
        UserRepository userRepository = new UserRepository();
        IAiTaskDao taskDao = mock(IAiTaskDao.class);
        IAiAgentDao agentDao = mock(IAiAgentDao.class);
        UserContext userContext = mock(UserContext.class);

        ReflectionTestUtils.setField(userRepository, "taskDao", taskDao);
        ReflectionTestUtils.setField(userRepository, "agentDao", agentDao);
        ReflectionTestUtils.setField(userRepository, "userContext", userContext);
        ReflectionTestUtils.setField(userRepository, "userDao", mock(IAiUserDao.class));
        ReflectionTestUtils.setField(userRepository, "apiDao", mock(IAiApiDao.class));
        ReflectionTestUtils.setField(userRepository, "modelDao", mock(IAiModelDao.class));
        ReflectionTestUtils.setField(userRepository, "mcpDao", mock(IAiMcpDao.class));

        when(userContext.getUserId()).thenReturn(2L);
        when(agentDao.queryAgentByAgentId("agent_u_1")).thenReturn(AiAgent.builder()
                .agentId("agent_u_1")
                .agentFrom(2L)
                .build());

        userRepository.taskInsert(SettingTaskDTO.builder()
                .agentId("agent_u_1")
                .taskCron("0 0/5 * * * ?")
                .taskDesc("demo")
                .taskParam("{\"maxRetry\":1,\"maxRound\":2,\"userMessage\":\"hi\"}")
                .taskStatus(1)
                .build(), "task_u_1");

        verify(taskDao, times(1)).insert(any(AiTask.class));
    }

    @Test
    void taskListShouldOnlyReturnCurrentUserTasks() {
        UserRepository userRepository = new UserRepository();
        IAiTaskDao taskDao = mock(IAiTaskDao.class);
        UserContext userContext = mock(UserContext.class);

        ReflectionTestUtils.setField(userRepository, "taskDao", taskDao);
        ReflectionTestUtils.setField(userRepository, "userContext", userContext);
        ReflectionTestUtils.setField(userRepository, "userDao", mock(IAiUserDao.class));
        ReflectionTestUtils.setField(userRepository, "apiDao", mock(IAiApiDao.class));
        ReflectionTestUtils.setField(userRepository, "modelDao", mock(IAiModelDao.class));
        ReflectionTestUtils.setField(userRepository, "mcpDao", mock(IAiMcpDao.class));
        ReflectionTestUtils.setField(userRepository, "agentDao", mock(IAiAgentDao.class));

        when(userContext.getUserId()).thenReturn(2L);
        when(taskDao.queryByTaskFrom(2L)).thenReturn(List.of(
                AiTask.builder().taskId("task_a").agentId("agent_a").taskCron("*").taskDesc("a").taskParam("{}").taskStatus(1).build()
        ));

        List<UserTaskVO> taskList = userRepository.taskList();

        assertEquals(1, taskList.size());
        assertEquals("task_a", taskList.get(0).getTaskId());
    }

    @Test
    void taskUpdateShouldRejectForeignTask() {
        UserRepository userRepository = new UserRepository();
        IAiTaskDao taskDao = mock(IAiTaskDao.class);
        UserContext userContext = mock(UserContext.class);

        ReflectionTestUtils.setField(userRepository, "taskDao", taskDao);
        ReflectionTestUtils.setField(userRepository, "userContext", userContext);
        ReflectionTestUtils.setField(userRepository, "userDao", mock(IAiUserDao.class));
        ReflectionTestUtils.setField(userRepository, "apiDao", mock(IAiApiDao.class));
        ReflectionTestUtils.setField(userRepository, "modelDao", mock(IAiModelDao.class));
        ReflectionTestUtils.setField(userRepository, "mcpDao", mock(IAiMcpDao.class));
        ReflectionTestUtils.setField(userRepository, "agentDao", mock(IAiAgentDao.class));

        when(userContext.getUserId()).thenReturn(2L);
        when(taskDao.queryByTaskIdAndFrom("task_x", 2L)).thenReturn(null);

        assertThrows(MiniAgentException.class, () -> userRepository.taskUpdate(SettingTaskDTO.builder()
                .taskId("task_x")
                .agentId("agent_x")
                .taskCron("*")
                .taskParam("{}")
                .build()));
    }

    @Test
    void taskDeleteShouldDeleteOwnedTask() {
        UserRepository userRepository = new UserRepository();
        IAiTaskDao taskDao = mock(IAiTaskDao.class);
        UserContext userContext = mock(UserContext.class);

        ReflectionTestUtils.setField(userRepository, "taskDao", taskDao);
        ReflectionTestUtils.setField(userRepository, "userContext", userContext);
        ReflectionTestUtils.setField(userRepository, "userDao", mock(IAiUserDao.class));
        ReflectionTestUtils.setField(userRepository, "apiDao", mock(IAiApiDao.class));
        ReflectionTestUtils.setField(userRepository, "modelDao", mock(IAiModelDao.class));
        ReflectionTestUtils.setField(userRepository, "mcpDao", mock(IAiMcpDao.class));
        ReflectionTestUtils.setField(userRepository, "agentDao", mock(IAiAgentDao.class));

        when(userContext.getUserId()).thenReturn(2L);
        when(taskDao.queryByTaskIdAndFrom("task_x", 2L)).thenReturn(AiTask.builder().id(9L).taskId("task_x").taskFrom(2L).build());

        userRepository.taskDelete("task_x");

        verify(taskDao, times(1)).delete(9L);
    }

    @Test
    void taskToggleShouldUpdateOwnedTaskStatus() {
        UserRepository userRepository = new UserRepository();
        IAiTaskDao taskDao = mock(IAiTaskDao.class);
        UserContext userContext = mock(UserContext.class);

        ReflectionTestUtils.setField(userRepository, "taskDao", taskDao);
        ReflectionTestUtils.setField(userRepository, "userContext", userContext);
        ReflectionTestUtils.setField(userRepository, "userDao", mock(IAiUserDao.class));
        ReflectionTestUtils.setField(userRepository, "apiDao", mock(IAiApiDao.class));
        ReflectionTestUtils.setField(userRepository, "modelDao", mock(IAiModelDao.class));
        ReflectionTestUtils.setField(userRepository, "mcpDao", mock(IAiMcpDao.class));
        ReflectionTestUtils.setField(userRepository, "agentDao", mock(IAiAgentDao.class));

        when(userContext.getUserId()).thenReturn(2L);
        when(taskDao.queryByTaskIdAndFrom("task_x", 2L)).thenReturn(AiTask.builder().id(9L).taskId("task_x").taskFrom(2L).taskStatus(1).build());

        userRepository.taskToggle("task_x", 0);

        verify(taskDao, times(1)).toggle(any(AiTask.class));
    }
}
