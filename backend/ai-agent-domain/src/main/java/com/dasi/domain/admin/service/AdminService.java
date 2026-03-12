package com.dasi.domain.admin.service;

import com.dasi.domain.admin.model.dto.*;
import com.dasi.domain.admin.model.enumeration.*;
import com.dasi.domain.admin.model.vo.*;
import com.dasi.domain.admin.repository.IAdminRepository;
import com.dasi.domain.admin.model.vo.DashboardVO;
import com.dasi.types.result.PageResult;
import com.dasi.types.exception.AdminException;
import com.dasi.types.exception.DependencyConflictException;
import jakarta.annotation.Resource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.dasi.types.constant.ExceptionMessage.*;

@Service
public class AdminService implements IAdminService {

    @Resource
    private IAdminRepository adminRepository;

    @Resource
    private PasswordEncoder passwordEncoder;

    // -------------------- Dashboard --------------------
    @Override
    public DashboardVO dashboard() {
        DashboardVO.CountInfo countInfo = adminRepository.dashboardCount();
        DashboardVO.GraphInfo graphInfo = adminRepository.dashboardChart();
        return DashboardVO.builder()
                .countInfo(countInfo)
                .graphInfo(graphInfo)
                .build();
    }


    // -------------------- API --------------------
    @Override
    public PageResult<ApiVO> apiPage(ApiPageDTO dto) {
        List<ApiVO> apiVOList = adminRepository.apiPage(dto);
        Integer total = adminRepository.apiCount(dto);
        Integer size = dto.getPageSize();
        Integer pageSum = (total + size - 1) / size;
        return PageResult.<ApiVO>builder()
                .list(apiVOList)
                .total(total)
                .pageSum(pageSum)
                .pageNum(dto.getPageNum())
                .pageSize(size)
                .build();
    }

    @Override
    public void apiInsert(ApiManageDTO dto) {
        if (adminRepository.apiQuery(dto.getApiId()) != null) {
            throw new AdminException(ADMIN_ALREADT_EXISTS);
        }
        adminRepository.apiInsert(dto);
    }

    @Override
    public void apiUpdate(ApiManageDTO dto) {
        if (adminRepository.apiQuery(dto.getApiId()) == null) {
            throw new AdminException(ADMIN_NOT_FOUND);
        }
        adminRepository.apiUpdate(dto);
    }

    @Override
    public void apiDelete(String apiId) {
        ApiVO apiVO = adminRepository.apiQuery(apiId);
        if (apiVO == null) {
            throw new AdminException(ADMIN_NOT_FOUND);
        }
        assertNoDependency("模型", adminRepository.queryModelDependOnApi(apiVO.getApiId()));
        adminRepository.apiDelete(apiId);
    }

    // -------------------- Model --------------------
    @Override
    public PageResult<ModelVO> modelPage(ModelPageDTO dto) {
        List<ModelVO> list = adminRepository.modelPage(dto);
        Integer total = adminRepository.modelCount(dto);
        Integer size = dto.getPageSize();
        Integer pageSum = (total + size - 1) / size;
        return PageResult.<ModelVO>builder()
                .list(list)
                .total(total)
                .pageSum(pageSum)
                .pageNum(dto.getPageNum())
                .pageSize(size)
                .build();
    }

    @Override
    public void modelInsert(ModelManageDTO dto) {
        if (adminRepository.modelQuery(dto.getModelId()) != null) {
            throw new AdminException(ADMIN_ALREADT_EXISTS);
        }
        adminRepository.modelInsert(dto);
    }

    @Override
    public void modelUpdate(ModelManageDTO dto) {
        if (adminRepository.modelQuery(dto.getModelId()) == null) {
            throw new AdminException(ADMIN_NOT_FOUND);
        }
        adminRepository.modelUpdate(dto);
    }

    @Override
    public void modelDelete(String modelId) {
        ModelVO modelVO = adminRepository.modelQuery(modelId);
        if (modelVO == null) {
            throw new AdminException(ADMIN_NOT_FOUND);
        }
        assertNoDependency("客户端", adminRepository.queryClientDependOnModel(modelVO.getModelId()));
        adminRepository.modelDelete(modelId);
    }

    // -------------------- MCP --------------------
    @Override
    public PageResult<McpVO> mcpPage(McpPageDTO dto) {
        List<McpVO> list = adminRepository.mcpPage(dto);
        Integer total = adminRepository.mcpCount(dto);
        Integer size = dto.getPageSize();
        Integer pageSum = (total + size - 1) / size;
        return PageResult.<McpVO>builder()
                .list(list)
                .total(total)
                .pageSum(pageSum)
                .pageNum(dto.getPageNum())
                .pageSize(size)
                .build();
    }

    @Override
    public void mcpInsert(McpManageDTO dto) {
        if (adminRepository.mcpQuery(dto.getMcpId()) != null) {
            throw new AdminException(ADMIN_ALREADT_EXISTS);
        }
        adminRepository.mcpInsert(dto);
    }

    @Override
    public void mcpUpdate(McpManageDTO dto) {
        if (adminRepository.mcpQuery(dto.getMcpId()) == null) {
            throw new AdminException(ADMIN_NOT_FOUND);
        }
        adminRepository.mcpUpdate(dto);
    }

    @Override
    public void mcpDelete(String mcpId) {
        McpVO mcpVO = adminRepository.mcpQuery(mcpId);
        if (mcpVO == null) {
            throw new AdminException(ADMIN_NOT_FOUND);
        }
        assertNoDependency("客户端", adminRepository.queryClientDependOnMcp(mcpVO.getMcpId()));
        adminRepository.mcpDelete(mcpId);
    }

    // -------------------- Advisor --------------------
    @Override
    public PageResult<AdvisorVO> advisorPage(AdvisorPageDTO dto) {
        List<AdvisorVO> list = adminRepository.advisorPage(dto);
        Integer total = adminRepository.advisorCount(dto);
        Integer size = dto.getPageSize();
        Integer pageSum = (total + size - 1) / size;
        return PageResult.<AdvisorVO>builder()
                .list(list)
                .total(total)
                .pageSum(pageSum)
                .pageNum(dto.getPageNum())
                .pageSize(size)
                .build();
    }

    @Override
    public void advisorInsert(AdvisorManageDTO dto) {
        if (adminRepository.advisorQuery(dto.getAdvisorId()) != null) {
            throw new AdminException(ADMIN_ALREADT_EXISTS);
        }
        adminRepository.advisorInsert(dto);
    }

    @Override
    public void advisorUpdate(AdvisorManageDTO dto) {
        if (adminRepository.advisorQuery(dto.getAdvisorId()) == null) {
            throw new AdminException(ADMIN_NOT_FOUND);
        }
        adminRepository.advisorUpdate(dto);
    }

    @Override
    public void advisorDelete(String advisorId) {
        AdvisorVO advisorVO = adminRepository.advisorQuery(advisorId);
        if (advisorVO == null) {
            throw new AdminException(ADMIN_NOT_FOUND);
        }
        assertNoDependency("客户端", adminRepository.queryClientDependOnAdvisor(advisorVO.getAdvisorId()));
        adminRepository.advisorDelete(advisorId);
    }

    // -------------------- Prompt --------------------
    @Override
    public PageResult<PromptVO> promptPage(PromptPageDTO dto) {
        List<PromptVO> list = adminRepository.promptPage(dto);
        Integer total = adminRepository.promptCount(dto);
        Integer size = dto.getPageSize();
        Integer pageSum = (total + size - 1) / size;
        return PageResult.<PromptVO>builder()
                .list(list)
                .total(total)
                .pageSum(pageSum)
                .pageNum(dto.getPageNum())
                .pageSize(size)
                .build();
    }

    @Override
    public void promptInsert(PromptManageDTO dto) {
        if (adminRepository.promptQuery(dto.getPromptId()) != null) {
            throw new AdminException(ADMIN_ALREADT_EXISTS);
        }
        adminRepository.promptInsert(dto);
    }

    @Override
    public void promptUpdate(PromptManageDTO dto) {
        if (adminRepository.promptQuery(dto.getPromptId()) == null) {
            throw new AdminException(ADMIN_NOT_FOUND);
        }
        adminRepository.promptUpdate(dto);
    }

    @Override
    public void promptDelete(String promptId) {
        PromptVO promptVO = adminRepository.promptQuery(promptId);
        if (promptVO == null) {
            throw new AdminException(ADMIN_NOT_FOUND);
        }
        assertNoDependency("客户端", adminRepository.queryClientDependOnPrompt(promptVO.getPromptId()));
        adminRepository.promptDelete(promptId);
    }

    // -------------------- Client --------------------
    @Override
    public PageResult<ClientVO> clientPage(ClientPageDTO dto) {
        List<ClientVO> list = adminRepository.clientPage(dto);
        Integer total = adminRepository.clientCount(dto);
        Integer size = dto.getPageSize();
        Integer pageSum = (total + size - 1) / size;
        return PageResult.<ClientVO>builder()
                .list(list)
                .total(total)
                .pageSum(pageSum)
                .pageNum(dto.getPageNum())
                .pageSize(size)
                .build();
    }

    @Override
    public void clientInsert(ClientManageDTO dto) {
        if (adminRepository.clientQuery(dto.getClientId()) != null) {
            throw new AdminException(ADMIN_ALREADT_EXISTS);
        }
        adminRepository.clientInsert(dto);
    }

    @Override
    public void clientUpdate(ClientManageDTO dto) {
        if (adminRepository.clientQuery(dto.getClientId()) == null) {
            throw new AdminException(ADMIN_NOT_FOUND);
        }
        adminRepository.clientUpdate(dto);
    }

    @Override
    public void clientDelete(String clientId) {
        ClientVO clientVO = adminRepository.clientQuery(clientId);
        if (clientVO == null) {
            throw new AdminException(ADMIN_NOT_FOUND);
        }
        adminRepository.clientDelete(clientId);
    }

    @Override
    public void clientToggle(String clientId, Integer status) {
        ClientVO clientVO = adminRepository.clientQuery(clientId);
        if (clientVO == null) {
            throw new AdminException(ADMIN_NOT_FOUND);
        }
        adminRepository.clientToggle(clientId, status);
    }

    // -------------------- Agent --------------------
    @Override
    public PageResult<AgentVO> agentPage(AgentPageDTO dto) {
        List<AgentVO> voList = adminRepository.agentPage(dto);
        Integer total = adminRepository.agentCount(dto);
        Integer size = dto.getPageSize();
        Integer pageSum = (total + size - 1) / size;
        return PageResult.<AgentVO>builder()
                .list(voList)
                .total(total)
                .pageSum(pageSum)
                .pageNum(dto.getPageNum())
                .pageSize(size)
                .build();
    }

    @Override
    public List<AgentVO> agentList(AgentListDTO dto) {
        return adminRepository.agentList(dto);
    }

    @Override
    public void agentInsert(AgentManageDTO dto) {
        if (adminRepository.agentQuery(dto.getAgentId()) != null) {
            throw new AdminException(ADMIN_ALREADT_EXISTS);
        }
        adminRepository.agentInsert(dto);
    }

    @Override
    public void agentUpdate(AgentManageDTO dto) {
        if (adminRepository.agentQuery(dto.getAgentId()) == null) {
            throw new AdminException(ADMIN_NOT_FOUND);
        }
        adminRepository.agentUpdate(dto);
    }

    @Override
    public void agentDelete(String agentId) {
        AgentVO agentVO = adminRepository.agentQuery(agentId);
        if (agentVO == null) {
            throw new AdminException(ADMIN_NOT_FOUND);
        }
        adminRepository.agentDelete(agentId);
    }

    @Override
    public void agentToggle(String agentId, Integer status) {
        AgentVO agentVO = adminRepository.agentQuery(agentId);
        if (agentVO == null) {
            throw new AdminException(ADMIN_NOT_FOUND);
        }
        adminRepository.agentToggle(agentId, status);
    }

    // -------------------- User --------------------
    @Override
    public PageResult<UserVO> userPage(UserPageDTO dto) {
        List<UserVO> voList = adminRepository.userPage(dto);
        Integer total = adminRepository.userCount(dto);
        Integer size = dto.getPageSize();
        Integer pageSum = (total + size - 1) / size;
        return PageResult.<UserVO>builder()
                .list(voList)
                .total(total)
                .pageSum(pageSum)
                .pageNum(dto.getPageNum())
                .pageSize(size)
                .build();
    }

    @Override
    public void userInsert(UserManageDTO dto) {
        if (adminRepository.userQuery(dto.getUserName()) != null) {
            throw new AdminException(ADMIN_ALREADT_EXISTS);
        }
        dto.setPassword(passwordEncoder.encode(dto.getPassword()));
        adminRepository.userInsert(dto);
    }

    @Override
    public void userUpdate(UserManageDTO dto) {
        String originUserName = dto.getOriginUserName() == null ? dto.getUserName() : dto.getOriginUserName();
        if (adminRepository.userQuery(originUserName) == null) {
            throw new AdminException(ADMIN_NOT_FOUND);
        }
        dto.setPassword(passwordEncoder.encode(dto.getPassword()));
        adminRepository.userUpdate(dto);
    }

    @Override
    public void userDelete(String userName) {
        if (adminRepository.userQuery(userName) == null) {
            throw new AdminException(ADMIN_NOT_FOUND);
        }
        adminRepository.userDelete(userName);
    }

    @Override
    public void userToggle(String userName, Integer status) {
        if (adminRepository.userQuery(userName) == null) {
            throw new AdminException(ADMIN_NOT_FOUND);
        }
        adminRepository.userToggle(userName, status);
    }

    // -------------------- Config --------------------
    @Override
    public Map<String, List<ConfigVO>> configList(ConfigListDTO dto) {
        List<ConfigVO> configVOList = adminRepository.configList(dto);
        return configVOList.stream()
                .collect(Collectors.groupingBy(
                        ConfigVO::getClientId,
                        LinkedHashMap::new,
                        Collectors.toList()
                ));
    }

    @Override
    public void configInsert(ConfigManageDTO dto) {
        if (adminRepository.configQuery(dto) != null) {
            throw new AdminException(ADMIN_ALREADT_EXISTS);
        }
        adminRepository.configInsert(dto);
    }

    @Override
    public void configUpdate(ConfigManageDTO dto) {
        ConfigManageDTO queryDTO = ConfigManageDTO.builder()
                .clientId(dto.getOriginClientId() == null ? dto.getClientId() : dto.getOriginClientId())
                .configType(dto.getOriginConfigType() == null ? dto.getConfigType() : dto.getOriginConfigType())
                .configValue(dto.getOriginConfigValue() == null ? dto.getConfigValue() : dto.getOriginConfigValue())
                .build();
        if (adminRepository.configQuery(queryDTO) == null) {
            throw new AdminException(ADMIN_NOT_FOUND);
        }
        adminRepository.configUpdate(dto);
    }

    @Override
    public void configDelete(String clientId, String configType, String configValue) {
        ConfigVO configVO = adminRepository.configQuery(ConfigManageDTO.builder()
                .clientId(clientId)
                .configType(configType)
                .configValue(configValue)
                .build());
        if (configVO == null) {
            throw new AdminException(ADMIN_NOT_FOUND);
        }
        assertNoDependency("客户端", adminRepository.queryAgentDependOnClient(configVO.getClientId()));
        adminRepository.configDelete(clientId, configType, configValue);
    }

    @Override
    public void configToggle(String clientId, String configType, String configValue, Integer status) {
        ConfigVO configVO = adminRepository.configQuery(ConfigManageDTO.builder()
                .clientId(clientId)
                .configType(configType)
                .configValue(configValue)
                .build());
        if (configVO == null) {
            throw new AdminException(ADMIN_NOT_FOUND);
        }
        if (status == 0) {
            assertNoDependency("客户端", adminRepository.queryAgentDependOnClient(configVO.getClientId()));
        }
        adminRepository.configToggle(clientId, configType, configValue, status);
    }


    // -------------------- Flow --------------------
    @Override
    public List<ClientDetailVO> flowClient() {
        return adminRepository.flowClient();
    }

    @Override
    public List<FlowVO> flowAgent(String agentId) {
        return adminRepository.flowAgent(agentId);
    }

    @Override
    public void flowInsert(FlowManageDTO dto) {
        if (adminRepository.flowQuery(dto.getAgentId(), dto.getClientId()) != null) {
            throw new AdminException(ADMIN_ALREADT_EXISTS);
        }
        adminRepository.flowInsert(dto);
    }

    @Override
    public void flowUpdate(FlowManageDTO dto) {
        String originAgentId = dto.getOriginAgentId() == null ? dto.getAgentId() : dto.getOriginAgentId();
        String originClientId = dto.getOriginClientId() == null ? dto.getClientId() : dto.getOriginClientId();
        if (adminRepository.flowQuery(originAgentId, originClientId) == null) {
            throw new AdminException(ADMIN_NOT_FOUND);
        }
        adminRepository.flowUpdate(dto);
    }

    @Override
    public void flowDelete(String agentId, String clientId) {
        if (adminRepository.flowQuery(agentId, clientId) == null) {
            throw new AdminException(ADMIN_NOT_FOUND);
        }
        adminRepository.flowDelete(agentId, clientId);
    }

    // -------------------- Task --------------------
    @Override
    public PageResult<TaskVO> taskPage(TaskPageDTO dto) {
        List<TaskVO> list = adminRepository.taskPage(dto);
        Integer total = adminRepository.taskCount(dto);
        Integer size = dto.getPageSize();
        Integer pageSum = (total + size - 1) / size;
        return PageResult.<TaskVO>builder()
                .list(list)
                .total(total)
                .pageSum(pageSum)
                .pageNum(dto.getPageNum())
                .pageSize(size)
                .build();
    }

    @Override
    public void taskInsert(TaskManageDTO dto) {
        if (adminRepository.taskQuery(dto.getTaskId()) != null) {
            throw new AdminException(ADMIN_ALREADT_EXISTS);
        }
        adminRepository.taskInsert(dto);
    }

    @Override
    public void taskUpdate(TaskManageDTO dto) {
        if (adminRepository.taskQuery(dto.getTaskId()) == null) {
            throw new AdminException(ADMIN_NOT_FOUND);
        }
        adminRepository.taskUpdate(dto);
    }

    @Override
    public void taskDelete(String taskId) {
        if (adminRepository.taskQuery(taskId) == null) {
            throw new AdminException(ADMIN_NOT_FOUND);
        }
        adminRepository.taskDelete(taskId);
    }

    @Override
    public void taskToggle(String taskId, Integer status) {
        if (adminRepository.taskQuery(taskId) == null) {
            throw new AdminException(ADMIN_NOT_FOUND);
        }
        adminRepository.taskToggle(taskId, status);
    }

    // -------------------- Session --------------------
    @Override
    public List<SessionVO> listSession() {
        return adminRepository.listSession();
    }

    // -------------------- Template --------------------
    @Override
    public PageResult<TemplateVO> templatePage(TemplatePageDTO dto) {
        List<TemplateVO> list = adminRepository.templatePage(dto);
        Integer total = adminRepository.templateCount(dto);
        Integer size = dto.getPageSize();
        Integer pageSum = (total + size - 1) / size;
        return PageResult.<TemplateVO>builder()
                .list(list)
                .total(total)
                .pageSum(pageSum)
                .pageNum(dto.getPageNum())
                .pageSize(size)
                .build();
    }

    @Override
    public void templateInsert(TemplateManageDTO dto) {
        if (adminRepository.templateQuery(dto.getTemplateId()) != null) {
            throw new AdminException(ADMIN_ALREADT_EXISTS);
        }
        adminRepository.templateInsert(dto);
    }

    @Override
    public void templateUpdate(TemplateManageDTO dto) {
        if (adminRepository.templateQuery(dto.getTemplateId()) == null) {
            throw new AdminException(ADMIN_NOT_FOUND);
        }
        adminRepository.templateUpdate(dto);
    }

    @Override
    public void templateDelete(String templateId) {
        TemplateVO templateVO = adminRepository.templateQuery(templateId);
        if (templateVO == null) {
            throw new AdminException(ADMIN_NOT_FOUND);
        }
        assertNoDependency("广场发布", adminRepository.queryPlazaDependOnTemplate(templateVO.getTemplateId()));
        adminRepository.templateDelete(templateId);
    }

    // -------------------- Plaza --------------------
    @Override
    public PageResult<PlazaVO> plazaPage(PlazaPageDTO dto) {
        List<PlazaVO> list = adminRepository.plazaPage(dto);
        Integer total = adminRepository.plazaCount(dto);
        Integer size = dto.getPageSize();
        Integer pageSum = (total + size - 1) / size;
        return PageResult.<PlazaVO>builder()
                .list(list)
                .total(total)
                .pageSum(pageSum)
                .pageNum(dto.getPageNum())
                .pageSize(size)
                .build();
    }

    @Override
    public void plazaInsert(PlazaManageDTO dto) {
        if (adminRepository.plazaQuery(dto.getPlazaId()) != null) {
            throw new AdminException(ADMIN_ALREADT_EXISTS);
        }
        adminRepository.plazaInsert(dto);
    }

    @Override
    public void plazaUpdate(PlazaManageDTO dto) {
        if (adminRepository.plazaQuery(dto.getPlazaId()) == null) {
            throw new AdminException(ADMIN_NOT_FOUND);
        }
        adminRepository.plazaUpdate(dto);
    }

    @Override
    public void plazaDelete(String plazaId) {
        if (adminRepository.plazaQuery(plazaId) == null) {
            throw new AdminException(ADMIN_NOT_FOUND);
        }
        adminRepository.plazaDelete(plazaId);
    }

    // -------------------- List --------------------
    @Override
    public List<String> listClientType() {
        return Arrays.stream(AiClientType.values())
                .map(AiClientType::getType)
                .toList();
    }

    @Override
    public List<String> listAgentType() {
        return Arrays.stream(AiAgentType.values())
                .map(AiAgentType::getType)
                .toList();
    }

    @Override
    public List<String> listClientRole() {
        return Arrays.stream(AiClientRole.values())
                .map(AiClientRole::getRole)
                .toList();
    }

    @Override
    public List<String> listUserRole() {
        return Arrays.stream(UserRole.values())
                .map(UserRole::getRole)
                .toList();
    }

    @Override
    public List<String> listConfigType() {
        return Arrays.stream(AiConfigType.values())
                .map(AiConfigType::getType)
                .toList();
    }

    @Override
    public List<String> listApiId() {
        return adminRepository.listApiId();
    }

    @Override
    public List<String> listModelId() {
        return adminRepository.listModelId();
    }


    private void assertNoDependency(String dependentName, List<String> dependents) {
        if (!CollectionUtils.isEmpty(dependents)) {
            throw new DependencyConflictException(String.format(ADMIN_DEPENDENCY_CONFLICT, dependentName, String.join(",", dependents)));
        }
    }

}
