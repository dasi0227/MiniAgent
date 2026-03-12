package com.dasi.infrastructure.repository;

import com.dasi.domain.admin.model.dto.*;
import com.dasi.domain.admin.model.enumeration.AiConfigType;
import com.dasi.domain.admin.model.vo.*;
import com.dasi.domain.admin.repository.IAdminRepository;
import com.dasi.infrastructure.persistent.dao.*;
import com.dasi.infrastructure.persistent.po.*;
import com.dasi.infrastructure.persistent.vo.MessageDailyCount;
import com.dasi.types.annotation.CacheEvict;
import com.dasi.types.annotation.Cacheable;
import com.dasi.domain.admin.model.vo.DashboardVO;
import com.dasi.types.enumeration.CacheType;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.dasi.domain.admin.model.enumeration.AiConfigType.*;
import static com.dasi.types.constant.RedisConstant.*;
import static com.dasi.types.constant.StatConstant.*;

@Repository
public class AdminRepository implements IAdminRepository {

    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Resource
    private IAiApiDao aiApiDao;

    @Resource
    private IAiModelDao aiModelDao;

    @Resource
    private IAiMcpDao aiMcpDao;

    @Resource
    private IAiAdvisorDao aiAdvisorDao;

    @Resource
    private IAiPromptDao aiPromptDao;

    @Resource
    private IAiClientDao aiClientDao;

    @Resource
    private IAiConfigDao aiConfigDao;

    @Resource
    private IAiAgentDao aiAgentDao;

    @Resource
    private IAiUserDao aiUserDao;

    @Resource
    private IAiFlowDao aiFlowDao;

    @Resource
    private IAiTaskDao aiTaskDao;

    @Resource
    private IAiTemplateDao aiTemplateDao;

    @Resource
    private IAiRepoDao aiRepoDao;

    @Resource
    private IAiSessionDao aiSessionDao;

    @Resource
    private IAiMessageDao aiMessageDao;

    @Resource
    private IAiStatDao aiStatDao;

    @Resource
    private IAiPlazaDao aiPlazaDao;

    @Resource
    private IAiPlazaLikeDao aiPlazaLikeDao;

    @Resource
    private IAiPlazaFavorDao aiPlazaFavorDao;

    @Resource
    private IAiPlazaCommentDao aiPlazaCommentDao;

    // -------------------- Dashboard --------------------
    @Override
    public DashboardVO.CountInfo dashboardCount() {
        return DashboardVO.CountInfo.builder()
                .apiCount(safeInt(aiApiDao.countAll()))
                .modelCount(safeInt(aiModelDao.countAll()))
                .clientCount(safeInt(aiClientDao.countAll()))
                .agentCount(safeInt(aiAgentDao.countAll()))
                .promptCount(safeInt(aiPromptDao.countAll()))
                .advisorCount(safeInt(aiAdvisorDao.countAll()))
                .mcpCount(safeInt(aiMcpDao.countAll()))
                .configCount(safeInt(aiConfigDao.countAll()))
                .flowCount(safeInt(aiFlowDao.countAll()))
                .userCount(safeInt(aiUserDao.countAll()))
                .sessionCount(safeInt(aiSessionDao.countAll()))
                .messageCount(safeInt(aiMessageDao.countAll()))
                .taskCount(safeInt(aiTaskDao.countAll()))
                .plazaCount(safeInt(aiPlazaDao.countAll()))
                .build();
    }

    private Integer safeInt(Number value) {
        return value == null ? 0 : value.intValue();
    }

    @Override
    public DashboardVO.GraphInfo dashboardChart() {
        List<DashboardVO.ChartValue> messageLastWeek = buildMessageChart(7);
        List<DashboardVO.ChartValue> messageLastMonth = buildMessageChart(30);

        Map<String, List<DashboardVO.BarValue>> workUsage = buildUsageMap(STAT_WORK);
        Map<String, List<DashboardVO.BarValue>> chatUsage = buildUsageMap(STAT_CHAT);

        DashboardVO.PieValue sessionWorkVsChat = DashboardVO.PieValue.builder()
                .workCount(safeInt(aiSessionDao.countByType(STAT_WORK)))
                .chatCount(safeInt(aiSessionDao.countByType(STAT_CHAT)))
                .build();

        return DashboardVO.GraphInfo.builder()
                .messageLastWeek(messageLastWeek)
                .messageLastMonth(messageLastMonth)
                .workUsage(workUsage)
                .chatUsage(chatUsage)
                .sessionWorkVsChat(sessionWorkVsChat)
                .build();
    }

    private Map<String, List<DashboardVO.BarValue>> buildUsageMap(String statCategory) {
        Map<String, List<DashboardVO.BarValue>> result = new LinkedHashMap<>();

        for (String key : STAT_USAGE_LIST) {
            List<DashboardVO.BarValue> list = aiStatDao.sumByCategoryAndKey(statCategory, key, STAT_TOP_N)
                    .stream()
                    .map(it -> DashboardVO.BarValue.builder()
                            .id(it.getStatValue())
                            .value(safeInt(it.getTotalCount()))
                            .build())
                    .toList();
            result.put(key, list);
        }

        return result;
    }

    private List<DashboardVO.ChartValue> buildMessageChart(int days) {
        LocalDate today = LocalDate.now();
        LocalDate startDate = today.minusDays(days - 1L);
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = today.plusDays(1).atStartOfDay();

        Map<String, Integer> countMap = aiMessageDao.countByDateRange(start, end)
                .stream()
                .collect(Collectors.toMap(
                        MessageDailyCount::getDay,
                        item -> safeInt(item.getCount())
                ));

        return startDate.datesUntil(today.plusDays(1))
                .map(d -> {
                    String key = d.format(dateTimeFormatter);
                    return DashboardVO.ChartValue.builder()
                            .date(key)
                            .count(countMap.getOrDefault(key, 0))
                            .build();
                })
                .toList();
    }


    // -------------------- API --------------------
    @Override
    @Cacheable(cachePrefix = ADMIN_API_PREFIX, cacheClass = ApiVO.class, cacheType = CacheType.LIST)
    public List<ApiVO> apiPage(ApiPageDTO apiPageDTO) {

        String idKeyword = apiPageDTO.getKeyword();
        Integer pageNum = apiPageDTO.getPageNum();
        Integer pageSize = apiPageDTO.getPageSize();

        Integer offset = (pageNum - 1) * pageSize;
        List<AiApi> poList = aiApiDao.page(idKeyword, offset, pageSize);
        if (CollectionUtils.isEmpty(poList)) {
            return List.of();
        }

        return poList.stream().map(this::toApiVO).toList();
    }

    @Override
    @Cacheable(cachePrefix = ADMIN_API_PREFIX, cacheClass = Integer.class, cacheType = CacheType.VALUE)
    public Integer apiCount(ApiPageDTO apiPageDTO) {
        return aiApiDao.count(apiPageDTO.getKeyword());
    }

    @Override
    @Cacheable(cachePrefix = ADMIN_API_PREFIX, cacheClass = ApiVO.class, cacheType = CacheType.VALUE)
    public ApiVO apiQuery(String apiId) {
        return toApiVO(aiApiDao.queryByApiId(apiId));
    }

    @Override
    @CacheEvict(keyPrefix = {"ai:", "query:", "admin:"})
    public void apiInsert(ApiManageDTO apiManageDTO) {
        AiApi aiApi = toApiPO(apiManageDTO);
        aiApiDao.insert(aiApi);
    }

    @Override
    @CacheEvict(keyPrefix = {"ai:", "query:", "admin:"})
    public void apiUpdate(ApiManageDTO apiManageDTO) {
        AiApi existed = aiApiDao.queryByApiId(apiManageDTO.getApiId());
        if (existed == null) {
            return;
        }
        AiApi aiApi = toApiPO(apiManageDTO);
        aiApi.setId(existed.getId());
        aiApiDao.update(aiApi);
    }

    @Override
    @CacheEvict(keyPrefix = {"ai:", "query:", "admin:"})
    public void apiDelete(String apiId) {
        aiApiDao.deleteByApiId(apiId);
    }

    // -------------------- Model --------------------
    @Override
    @Cacheable(cachePrefix = ADMIN_MODEL_PREFIX, cacheClass = ModelVO.class, cacheType = CacheType.LIST)
    public List<ModelVO> modelPage(ModelPageDTO dto) {
        String keyword = dto.getKeyword();
        String apiId = dto.getApiId();
        Integer pageNum = dto.getPageNum();
        Integer pageSize = dto.getPageSize();
        Integer offset = (pageNum - 1) * pageSize;

        List<AiModel> poList = aiModelDao.page(keyword, apiId, offset, pageSize);
        if (CollectionUtils.isEmpty(poList)) {
            return List.of();
        }

        return poList.stream().map(this::toModelVO).toList();
    }

    @Override
    @Cacheable(cachePrefix = ADMIN_MODEL_PREFIX, cacheClass = Integer.class, cacheType = CacheType.VALUE)
    public Integer modelCount(ModelPageDTO dto) {
        return aiModelDao.count(dto.getKeyword(), dto.getApiId());
    }

    @Override
    @Cacheable(cachePrefix = ADMIN_MODEL_PREFIX, cacheClass = ModelVO.class, cacheType = CacheType.VALUE)
    public ModelVO modelQuery(String modelId) {
        return toModelVO(aiModelDao.queryByModelId(modelId));
    }

    @Override
    @CacheEvict(keyPrefix = {"ai:", "query:", "admin:"})
    public void modelInsert(ModelManageDTO dto) {
        aiModelDao.insert(toModelPo(dto));
    }

    @Override
    @CacheEvict(keyPrefix = {"ai:", "query:", "admin:"})
    public void modelUpdate(ModelManageDTO dto) {
        AiModel existed = aiModelDao.queryByModelId(dto.getModelId());
        if (existed == null) {
            return;
        }
        AiModel po = toModelPo(dto);
        po.setId(existed.getId());
        aiModelDao.update(po);
    }

    @Override
    @CacheEvict(keyPrefix = {"ai:", "query:", "admin:"})
    public void modelDelete(String modelId) {
        aiModelDao.deleteByModelId(modelId);
    }

    // -------------------- MCP --------------------
    @Override
    @Cacheable(cachePrefix = ADMIN_MCP_PREFIX, cacheClass = McpVO.class, cacheType = CacheType.LIST)
    public List<McpVO> mcpPage(McpPageDTO dto) {
        Integer offset = (dto.getPageNum() - 1) * dto.getPageSize();
        List<AiMcp> poList = aiMcpDao.page(dto.getKeyword(), offset, dto.getPageSize());
        if (CollectionUtils.isEmpty(poList)) {
            return List.of();
        }
        return poList.stream().map(this::toMcpVO).toList();
    }

    @Override
    @Cacheable(cachePrefix = ADMIN_MCP_PREFIX, cacheClass = Integer.class, cacheType = CacheType.VALUE)
    public Integer mcpCount(McpPageDTO dto) {
        return aiMcpDao.count(dto.getKeyword());
    }

    @Override
    @Cacheable(cachePrefix = ADMIN_MCP_PREFIX, cacheClass = McpVO.class, cacheType = CacheType.VALUE)
    public McpVO mcpQuery(String mcpId) {
        return toMcpVO(aiMcpDao.queryByMcpId(mcpId));
    }

    @Override
    @CacheEvict(keyPrefix = {"ai:", "query:", "admin:"})
    public void mcpInsert(McpManageDTO dto) {
        aiMcpDao.insert(toMcpPo(dto));
    }

    @Override
    @CacheEvict(keyPrefix = {"ai:", "query:", "admin:"})
    public void mcpUpdate(McpManageDTO dto) {
        AiMcp existed = aiMcpDao.queryByMcpId(dto.getMcpId());
        if (existed == null) {
            return;
        }
        AiMcp po = toMcpPo(dto);
        po.setId(existed.getId());
        aiMcpDao.update(po);
    }

    @Override
    @CacheEvict(keyPrefix = {"ai:", "query:", "admin:"})
    public void mcpDelete(String mcpId) {
        aiMcpDao.deleteByMcpId(mcpId);
    }

    // -------------------- Advisor --------------------
    @Override
    @Cacheable(cachePrefix = ADMIN_ADVISOR_PREFIX, cacheClass = AdvisorVO.class, cacheType = CacheType.LIST)
    public List<AdvisorVO> advisorPage(AdvisorPageDTO dto) {
        Integer offset = (dto.getPageNum() - 1) * dto.getPageSize();
        List<AiAdvisor> poList = aiAdvisorDao.page(dto.getKeyword(), offset, dto.getPageSize());
        if (CollectionUtils.isEmpty(poList)) {
            return List.of();
        }
        return poList.stream().map(this::toAdvisorVO).toList();
    }

    @Override
    @Cacheable(cachePrefix = ADMIN_ADVISOR_PREFIX, cacheClass = Integer.class, cacheType = CacheType.VALUE)
    public Integer advisorCount(AdvisorPageDTO dto) {
        return aiAdvisorDao.count(dto.getKeyword());
    }

    @Override
    @Cacheable(cachePrefix = ADMIN_ADVISOR_PREFIX, cacheClass = AdvisorVO.class, cacheType = CacheType.VALUE)
    public AdvisorVO advisorQuery(String advisorId) {
        return toAdvisorVO(aiAdvisorDao.queryByAdvisorId(advisorId));
    }

    @Override
    @CacheEvict(keyPrefix = {"ai:", "query:", "admin:"})
    public void advisorInsert(AdvisorManageDTO dto) {
        aiAdvisorDao.insert(toAdvisorPo(dto));
    }

    @Override
    @CacheEvict(keyPrefix = {"ai:", "query:", "admin:"})
    public void advisorUpdate(AdvisorManageDTO dto) {
        AiAdvisor existed = aiAdvisorDao.queryByAdvisorId(dto.getAdvisorId());
        if (existed == null) {
            return;
        }
        AiAdvisor po = toAdvisorPo(dto);
        po.setId(existed.getId());
        aiAdvisorDao.update(po);
    }

    @Override
    @CacheEvict(keyPrefix = {"ai:", "query:", "admin:"})
    public void advisorDelete(String advisorId) {
        AiAdvisor po = aiAdvisorDao.queryByAdvisorId(advisorId);
        if (po != null) {
            aiAdvisorDao.delete(po.getId());
        }
    }

    // -------------------- Prompt --------------------
    @Override
    @Cacheable(cachePrefix = ADMIN_PROMPT_PREFIX, cacheClass = PromptVO.class, cacheType = CacheType.LIST)
    public List<PromptVO> promptPage(PromptPageDTO dto) {
        Integer offset = (dto.getPageNum() - 1) * dto.getPageSize();
        List<AiPrompt> poList = aiPromptDao.page(dto.getKeyword(), offset, dto.getPageSize());
        if (CollectionUtils.isEmpty(poList)) {
            return List.of();
        }
        return poList.stream().map(this::toPromptVO).toList();
    }

    @Override
    @Cacheable(cachePrefix = ADMIN_PROMPT_PREFIX, cacheClass = Integer.class, cacheType = CacheType.VALUE)
    public Integer promptCount(PromptPageDTO dto) {
        return aiPromptDao.count(dto.getKeyword());
    }

    @Override
    @Cacheable(cachePrefix = ADMIN_PROMPT_PREFIX, cacheClass = PromptVO.class, cacheType = CacheType.VALUE)
    public PromptVO promptQuery(String promptId) {
        return toPromptVO(aiPromptDao.queryByPromptId(promptId));
    }

    @Override
    @CacheEvict(keyPrefix = {"ai:", "query:", "admin:"})
    public void promptInsert(PromptManageDTO dto) {
        aiPromptDao.insert(toPromptPo(dto));
    }

    @Override
    @CacheEvict(keyPrefix = {"ai:", "query:", "admin:"})
    public void promptUpdate(PromptManageDTO dto) {
        AiPrompt existed = aiPromptDao.queryByPromptId(dto.getPromptId());
        if (existed == null) {
            return;
        }
        AiPrompt po = toPromptPo(dto);
        po.setId(existed.getId());
        aiPromptDao.update(po);
    }

    @Override
    @CacheEvict(keyPrefix = {"ai:", "query:", "admin:"})
    public void promptDelete(String promptId) {
        AiPrompt po = aiPromptDao.queryByPromptId(promptId);
        if (po != null) {
            aiPromptDao.delete(po.getId());
        }
    }

    // -------------------- Client --------------------
    @Override
    @Cacheable(cachePrefix = ADMIN_CLIENT_PREFIX, cacheClass = ClientVO.class, cacheType = CacheType.LIST)
    public List<ClientVO> clientPage(ClientPageDTO dto) {
        Integer offset = (dto.getPageNum() - 1) * dto.getPageSize();
        List<AiClient> poList = aiClientDao.page(dto.getKeyword(), dto.getModelId(), "chat", dto.getClientRole(), offset, dto.getPageSize());
        if (CollectionUtils.isEmpty(poList)) {
            return List.of();
        }
        return poList.stream().map(this::toClientVO).toList();
    }

    @Override
    @Cacheable(cachePrefix = ADMIN_CLIENT_PREFIX, cacheClass = Integer.class, cacheType = CacheType.VALUE)
    public Integer clientCount(ClientPageDTO dto) {
        return aiClientDao.count(dto.getKeyword(), dto.getModelId(), "chat", dto.getClientRole());
    }

    @Override
    @Cacheable(cachePrefix = ADMIN_CLIENT_PREFIX, cacheClass = ClientVO.class, cacheType = CacheType.VALUE)
    public ClientVO clientQuery(String clientId) {
        return toClientVO(aiClientDao.queryByClientId(clientId));
    }

    @Override
    @CacheEvict(keyPrefix = {"ai:", "query:", "admin:"})
    public void clientInsert(ClientManageDTO dto) {
        aiClientDao.insert(toClientPo(dto));
    }

    @Override
    @CacheEvict(keyPrefix = {"ai:", "query:", "admin:"})
    public void clientUpdate(ClientManageDTO dto) {
        AiClient existed = aiClientDao.queryByClientId(dto.getClientId());
        if (existed == null) {
            return;
        }
        AiClient po = toClientPo(dto);
        po.setId(existed.getId());
        aiClientDao.update(po);
    }

    @Override
    @CacheEvict(keyPrefix = {"ai:", "query:", "admin:"})
    public void clientDelete(String clientId) {
        aiConfigDao.deleteByClientId(clientId);
        aiFlowDao.deleteByClientId(clientId);
        aiClientDao.deleteByClientId(clientId);
    }

    @Override
    @CacheEvict(keyPrefix = {"ai:", "query:", "admin:"})
    public void clientToggle(String clientId, Integer status) {
        AiClient existed = aiClientDao.queryByClientId(clientId);
        if (existed == null) {
            return;
        }
        AiClient po = AiClient.builder()
                .id(existed.getId())
                .clientStatus(status)
                .build();
        aiClientDao.toggle(po);
    }

    // -------------------- Agent --------------------
    @Override
    @Cacheable(cachePrefix = ADMIN_AGENT_PREFIX, cacheClass = AgentVO.class, cacheType = CacheType.LIST)
    public List<AgentVO> agentPage(AgentPageDTO dto) {
        Integer offset = (dto.getPageNum() - 1) * dto.getPageSize();
        List<AiAgent> poList = aiAgentDao.page(dto.getKeyword(), dto.getAgentType(), offset, dto.getPageSize());
        if (CollectionUtils.isEmpty(poList)) {
            return List.of();
        }
        return poList.stream().map(this::toAgentVO).toList();
    }

    @Override
    @Cacheable(cachePrefix = ADMIN_AGENT_PREFIX, cacheClass = AgentVO.class, cacheType = CacheType.LIST)
    public List<AgentVO> agentList(AgentListDTO dto) {
        List<AiAgent> poList = aiAgentDao.list(dto.getKeyword(), dto.getAgentType());
        return poList.stream().map(this::toAgentVO).toList();
    }

    @Override
    @Cacheable(cachePrefix = ADMIN_AGENT_PREFIX, cacheClass = Integer.class, cacheType = CacheType.VALUE)
    public Integer agentCount(AgentPageDTO dto) {
        return aiAgentDao.count(dto.getKeyword(), dto.getAgentType());
    }

    @Override
    @Cacheable(cachePrefix = ADMIN_AGENT_PREFIX, cacheClass = AgentVO.class, cacheType = CacheType.VALUE)
    public AgentVO agentQuery(String agentId) {
        return toAgentVO(aiAgentDao.queryAgentByAgentId(agentId));
    }

    @Override
    @CacheEvict(keyPrefix = {"ai:", "query:", "admin:"})
    public void agentInsert(AgentManageDTO dto) {
        aiAgentDao.insert(toAgentPo(dto));
    }

    @Override
    @CacheEvict(keyPrefix = {"ai:", "query:", "admin:"})
    public void agentUpdate(AgentManageDTO dto) {
        AiAgent existed = aiAgentDao.queryAgentByAgentId(dto.getAgentId());
        if (existed == null) {
            return;
        }
        AiAgent po = toAgentPo(dto);
        po.setId(existed.getId());
        aiAgentDao.update(po);
    }

    @Override
    @CacheEvict(keyPrefix = {"ai:", "query:", "admin:"})
    public void agentDelete(String agentId) {
        AiAgent aiAgent = aiAgentDao.queryAgentByAgentId(agentId);
        if (aiAgent == null) {
            return;
        }

        List<AiFlow> aiFlowList = aiFlowDao.queryByAgentId(agentId);
        Set<String> clientIdSet = aiFlowList.stream()
                .map(AiFlow::getClientId)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        for (String clientId : clientIdSet) {
            aiConfigDao.deleteByClientId(clientId);
            aiFlowDao.deleteByClientId(clientId);
            aiClientDao.deleteByClientId(clientId);
        }

        aiTaskDao.deleteByAgentId(agentId);
        aiFlowDao.deleteByAgentId(agentId);
        aiRepoDao.deleteByAgentId(agentId);
        aiAgentDao.deleteByAgentId(agentId);

        String templateId = aiAgent.getTemplateId();
        if (templateId != null && !templateId.isBlank()) {
            List<AiPlaza> aiPlazaList = aiPlazaDao.listByTemplateId(templateId);
            for (AiPlaza aiPlaza : aiPlazaList) {
                String plazaId = aiPlaza.getPlazaId();
                aiPlazaLikeDao.deleteByPlazaId(plazaId);
                aiPlazaFavorDao.deleteByPlazaId(plazaId);
                aiPlazaCommentDao.deleteByPlazaId(plazaId);
                aiPlazaDao.deleteByPlazaId(plazaId);
            }
            aiRepoDao.deleteByTemplateId(templateId);
            aiTemplateDao.deleteByTemplateId(templateId);
        }
    }

    @Override
    @CacheEvict(keyPrefix = {"ai:", "query:", "admin:"})
    public void agentToggle(String agentId, Integer status) {
        AiAgent existed = aiAgentDao.queryAgentByAgentId(agentId);
        if (existed == null) {
            return;
        }
        AiAgent po = AiAgent.builder()
                .id(existed.getId())
                .agentStatus(status)
                .build();
        aiAgentDao.toggle(po);
    }

    // -------------------- AiUser --------------------
    @Override
    @Cacheable(cachePrefix = ADMIN_USER_PREFIX, cacheClass = UserVO.class, cacheType = CacheType.LIST)
    public List<UserVO> userPage(UserPageDTO dto) {
        Integer offset = (dto.getPageNum() - 1) * dto.getPageSize();
        List<AiUser> poList = aiUserDao.page(dto.getKeyword(), dto.getUserRole(), offset, dto.getPageSize());
        if (CollectionUtils.isEmpty(poList)) {
            return List.of();
        }
        return poList.stream().map(this::toUserVO).toList();
    }

    @Override
    @Cacheable(cachePrefix = ADMIN_USER_PREFIX, cacheClass = Integer.class, cacheType = CacheType.VALUE)
    public Integer userCount(UserPageDTO dto) {
        Long count = aiUserDao.count(dto.getKeyword(), dto.getUserRole());
        return count == null ? 0 : count.intValue();
    }

    @Override
    @Cacheable(cachePrefix = ADMIN_USER_PREFIX, cacheClass = UserVO.class, cacheType = CacheType.VALUE)
    public UserVO userQuery(String userName) {
        return toUserVO(aiUserDao.queryByUserName(userName));
    }

    @Override
    @CacheEvict(keyPrefix = {"ai:", "query:", "admin:"})
    public void userInsert(UserManageDTO dto) {
        aiUserDao.insert(toUserPo(dto));
    }

    @Override
    @CacheEvict(keyPrefix = {"ai:", "query:", "admin:"})
    public void userUpdate(UserManageDTO dto) {
        String originUserName = dto.getOriginUserName() == null ? dto.getUserName() : dto.getOriginUserName();
        AiUser existed = aiUserDao.queryByUserName(originUserName);
        if (existed == null) {
            return;
        }
        AiUser po = toUserPo(dto);
        po.setId(existed.getId());
        aiUserDao.update(po);
    }

    @Override
    @CacheEvict(keyPrefix = {"ai:", "query:", "admin:"})
    public void userDelete(String userName) {
        AiUser existed = aiUserDao.queryByUserName(userName);
        if (existed != null) {
            aiUserDao.delete(existed.getId());
        }
    }

    @Override
    @CacheEvict(keyPrefix = {"ai:", "query:", "admin:"})
    public void userToggle(String userName, Integer status) {
        AiUser existed = aiUserDao.queryByUserName(userName);
        if (existed != null) {
            aiUserDao.toggle(existed.getId(), status);
        }
    }

    // -------------------- Config --------------------
    @Override
    @Cacheable(cachePrefix = ADMIN_CONFIG_PREFIX, cacheType = CacheType.LIST, cacheClass = ConfigVO.class)
    public List<ConfigVO> configList(ConfigListDTO dto) {
        List<AiConfig> poList = aiConfigDao.list(dto.getKeyword(), dto.getConfigType());
        return poList.stream().map(this::toConfigVO).toList();
    }

    @Override
    @Cacheable(cachePrefix = ADMIN_CONFIG_PREFIX, cacheType = CacheType.VALUE, cacheClass = ConfigVO.class)
    public ConfigVO configQuery(ConfigManageDTO dto) {
        return toConfigVO(aiConfigDao.queryByUniqueKey(dto.getClientId(), dto.getConfigType(), dto.getConfigValue()));
    }

    @Override
    @CacheEvict(keyPrefix = {"ai:", "query:", "admin:"})
    public void configInsert(ConfigManageDTO dto) {
        aiConfigDao.insert(toConfigPO(dto));
    }

    @Override
    @CacheEvict(keyPrefix = {"ai:", "query:", "admin:"})
    public void configUpdate(ConfigManageDTO dto) {
        String originClientId = dto.getOriginClientId() == null ? dto.getClientId() : dto.getOriginClientId();
        String originConfigType = dto.getOriginConfigType() == null ? dto.getConfigType() : dto.getOriginConfigType();
        String originConfigValue = dto.getOriginConfigValue() == null ? dto.getConfigValue() : dto.getOriginConfigValue();
        AiConfig existed = aiConfigDao.queryByUniqueKey(originClientId, originConfigType, originConfigValue);
        if (existed == null) {
            return;
        }
        AiConfig po = toConfigPO(dto);
        po.setId(existed.getId());
        aiConfigDao.update(po);
    }

    @Override
    @CacheEvict(keyPrefix = {"ai:", "query:", "admin:"})
    public void configDelete(String clientId, String configType, String configValue) {
        AiConfig existed = aiConfigDao.queryByUniqueKey(clientId, configType, configValue);
        if (existed != null) {
            aiConfigDao.delete(existed.getId());
        }
    }

    @Override
    @CacheEvict(keyPrefix = {"ai:", "query:", "admin:"})
    public void configToggle(String clientId, String configType, String configValue, Integer status) {
        AiConfig existed = aiConfigDao.queryByUniqueKey(clientId, configType, configValue);
        if (existed != null) {
            aiConfigDao.toggle(existed.getId(), status);
        }
    }

    // -------------------- Flow --------------------
    @Override
    @Cacheable(cachePrefix = ADMIN_FLOW_PREFIX, cacheType = CacheType.LIST, cacheClass = ClientDetailVO.class)
    public List<ClientDetailVO> flowClient() {
        List<ClientDetailVO> clientDetailVOList = new ArrayList<>();

        List<AiClient> aiClientList = aiClientDao.queryWorkClientList();
        if (aiClientList.isEmpty()) {
            return clientDetailVOList;
        }

        for (AiClient aiClient : aiClientList) {
            String clientId = aiClient.getClientId();
            String clientRole = aiClient.getClientRole();
            ClientVO clientVO = toClientVO(aiClient);

            String modelId = aiClient.getModelId();
            AiModel aiModel = aiModelDao.queryByModelId(modelId);
            ModelVO modelVO = toModelVO(aiModel);

            String apiId = aiModel.getApiId();
            AiApi aiApi = aiApiDao.queryByApiId(apiId);
            ApiVO apiVO = toApiVO(aiApi);

            List<AiConfig> aiConfigList = aiConfigDao.queryByClientId(clientId);
            if (aiConfigList.isEmpty()) {
                continue;
            }

            List<McpVO> mcpVOList = new ArrayList<>();
            List<AdvisorVO> advisorVOList = new ArrayList<>();
            List<PromptVO> promptVOList = new ArrayList<>();
            for (AiConfig aiConfig : aiConfigList) {
                switch (AiConfigType.fromString(aiConfig.getConfigType())) {
                    case MCP -> {
                        AiMcp aiMcp = aiMcpDao.queryByMcpId(aiConfig.getConfigValue());
                        mcpVOList.add(toMcpVO(aiMcp));
                    }
                    case ADVISOR -> {
                        AiAdvisor aiAdvisor = aiAdvisorDao.queryByAdvisorId(aiConfig.getConfigValue());
                        advisorVOList.add(toAdvisorVO(aiAdvisor));
                    }
                    case PROMPT -> {
                        AiPrompt aiPrompt = aiPromptDao.queryByPromptId(aiConfig.getConfigValue());
                        promptVOList.add(toPromptVO(aiPrompt));
                    }
                }
            }

            ClientDetailVO clientDetailVO = ClientDetailVO.builder()
                    .clientId(clientId)
                    .clientRole(clientRole)
                    .client(clientVO)
                    .model(modelVO)
                    .api(apiVO)
                    .mcpList(mcpVOList)
                    .advisorList(advisorVOList)
                    .promptList(promptVOList)
                    .build();

            clientDetailVOList.add(clientDetailVO);
        }

        return clientDetailVOList;
    }

    @Override
    @Cacheable(cachePrefix = ADMIN_FLOW_PREFIX, cacheType = CacheType.LIST, cacheClass = FlowVO.class)
    public List<FlowVO> flowAgent(String agentId) {
        List<AiFlow> poList = aiFlowDao.queryByAgentId(agentId);
        return poList.stream().map(this::toFlowVO).toList();
    }

    @Override
    @Cacheable(cachePrefix = ADMIN_FLOW_PREFIX, cacheType = CacheType.VALUE, cacheClass = FlowVO.class)
    public FlowVO flowQuery(String agentId, String clientId) {
        return toFlowVO(aiFlowDao.queryByAgentIdAndClientId(agentId, clientId));
    }

    @Override
    @CacheEvict(keyPrefix = {"ai:", "query:", "admin:"})
    public void flowInsert(FlowManageDTO dto) {
        aiFlowDao.insert(toFlowPO(dto));
    }

    @Override
    @CacheEvict(keyPrefix = {"ai:", "query:", "admin:"})
    public void flowUpdate(FlowManageDTO dto) {
        String originAgentId = dto.getOriginAgentId() == null ? dto.getAgentId() : dto.getOriginAgentId();
        String originClientId = dto.getOriginClientId() == null ? dto.getClientId() : dto.getOriginClientId();
        AiFlow existed = aiFlowDao.queryByAgentIdAndClientId(originAgentId, originClientId);
        if (existed == null) {
            return;
        }
        AiFlow po = toFlowPO(dto);
        po.setId(existed.getId());
        aiFlowDao.update(po);
    }

    @Override
    @CacheEvict(keyPrefix = {"ai:", "query:", "admin:"})
    public void flowDelete(String agentId, String clientId) {
        AiFlow existed = aiFlowDao.queryByAgentIdAndClientId(agentId, clientId);
        if (existed != null) {
            aiFlowDao.delete(existed.getId());
        }
    }

    // -------------------- Task --------------------
    @Override
    @Cacheable(cachePrefix = ADMIN_TASK_PREFIX, cacheType = CacheType.LIST, cacheClass = TaskVO.class)
    public List<TaskVO> taskPage(TaskPageDTO dto) {
        Integer offset = (dto.getPageNum() - 1) * dto.getPageSize();
        List<AiTask> poList = aiTaskDao.page(dto.getKeyword(), dto.getAgentId(), offset, dto.getPageSize());
        if (CollectionUtils.isEmpty(poList)) {
            return List.of();
        }
        return poList.stream().map(this::toTaskVO).toList();
    }

    @Override
    @Cacheable(cachePrefix = ADMIN_TASK_PREFIX, cacheType = CacheType.VALUE, cacheClass = Integer.class)
    public Integer taskCount(TaskPageDTO dto) {
        return aiTaskDao.count(dto.getKeyword(), dto.getAgentId());
    }

    @Override
    @Cacheable(cachePrefix = ADMIN_TASK_PREFIX, cacheType = CacheType.VALUE, cacheClass = TaskVO.class)
    public TaskVO taskQuery(String taskId) {
        return toTaskVO(aiTaskDao.queryByTaskId(taskId));
    }

    @Override
    @CacheEvict(keyPrefix = {"ai:", "query:", "admin:"})
    public void taskInsert(TaskManageDTO dto) {
        aiTaskDao.insert(toTaskPO(dto));
    }

    @Override
    @CacheEvict(keyPrefix = {"ai:", "query:", "admin:"})
    public void taskUpdate(TaskManageDTO dto) {
        AiTask existed = aiTaskDao.queryByTaskId(dto.getTaskId());
        if (existed == null) {
            return;
        }
        AiTask po = toTaskPO(dto);
        po.setId(existed.getId());
        aiTaskDao.update(po);
    }

    @Override
    @CacheEvict(keyPrefix = {"ai:", "query:", "admin:"})
    public void taskDelete(String taskId) {
        AiTask existed = aiTaskDao.queryByTaskId(taskId);
        if (existed != null) {
            aiTaskDao.delete(existed.getId());
        }
    }

    @Override
    @CacheEvict(keyPrefix = {"ai:", "query:", "admin:"})
    public void taskToggle(String taskId, Integer status) {
        AiTask existed = aiTaskDao.queryByTaskId(taskId);
        if (existed == null) {
            return;
        }
        AiTask po = AiTask.builder()
                .id(existed.getId())
                .taskStatus(status)
                .build();
        aiTaskDao.toggle(po);
    }

    // -------------------- AiSession --------------------
    @Override
    public List<SessionVO> listSession() {
        List<AiSession> list = aiSessionDao.queryAll();
        return list.stream().map(this::toSessionVO).toList();
    }

    // -------------------- Template --------------------
    @Override
    @Cacheable(cachePrefix = ADMIN_TEMPLATE_PREFIX, cacheType = CacheType.LIST, cacheClass = TemplateVO.class)
    public List<TemplateVO> templatePage(TemplatePageDTO dto) {
        Integer offset = (dto.getPageNum() - 1) * dto.getPageSize();
        List<AiTemplate> poList = aiTemplateDao.page(dto.getKeyword(), offset, dto.getPageSize());
        if (CollectionUtils.isEmpty(poList)) {
            return List.of();
        }
        return poList.stream().map(this::toTemplateVO).toList();
    }

    @Override
    @Cacheable(cachePrefix = ADMIN_TEMPLATE_PREFIX, cacheType = CacheType.VALUE, cacheClass = Integer.class)
    public Integer templateCount(TemplatePageDTO dto) {
        return aiTemplateDao.count(dto.getKeyword());
    }

    @Override
    @Cacheable(cachePrefix = ADMIN_TEMPLATE_PREFIX, cacheType = CacheType.VALUE, cacheClass = TemplateVO.class)
    public TemplateVO templateQuery(String templateId) {
        return toTemplateVO(aiTemplateDao.queryByTemplateId(templateId));
    }

    @Override
    @CacheEvict(keyPrefix = {"ai:", "query:", "admin:"})
    public void templateInsert(TemplateManageDTO dto) {
        aiTemplateDao.insert(toTemplatePO(dto));
    }

    @Override
    @CacheEvict(keyPrefix = {"ai:", "query:", "admin:"})
    public void templateUpdate(TemplateManageDTO dto) {
        AiTemplate existed = aiTemplateDao.queryByTemplateId(dto.getTemplateId());
        if (existed == null) {
            return;
        }
        AiTemplate po = toTemplatePO(dto);
        po.setId(existed.getId());
        aiTemplateDao.update(po);
    }

    @Override
    @CacheEvict(keyPrefix = {"ai:", "query:", "admin:"})
    public void templateDelete(String templateId) {
        aiRepoDao.deleteByTemplateId(templateId);
        aiTemplateDao.deleteByTemplateId(templateId);
    }

    // -------------------- Plaza --------------------
    @Override
    @Cacheable(cachePrefix = ADMIN_PLAZA_PREFIX, cacheType = CacheType.LIST, cacheClass = PlazaVO.class)
    public List<PlazaVO> plazaPage(PlazaPageDTO dto) {
        Integer offset = (dto.getPageNum() - 1) * dto.getPageSize();
        List<AiPlaza> poList = aiPlazaDao.page(dto.getKeyword(), dto.getSortBy(), dto.getSortOrder(), offset, dto.getPageSize());
        if (CollectionUtils.isEmpty(poList)) {
            return List.of();
        }
        return poList.stream().map(this::toPlazaVO).toList();
    }

    @Override
    @Cacheable(cachePrefix = ADMIN_PLAZA_PREFIX, cacheType = CacheType.VALUE, cacheClass = Integer.class)
    public Integer plazaCount(PlazaPageDTO dto) {
        return aiPlazaDao.count(dto.getKeyword());
    }

    @Override
    @Cacheable(cachePrefix = ADMIN_PLAZA_PREFIX, cacheType = CacheType.VALUE, cacheClass = PlazaVO.class)
    public PlazaVO plazaQuery(String plazaId) {
        return toPlazaVO(aiPlazaDao.queryByPlazaId(plazaId));
    }

    @Override
    @CacheEvict(keyPrefix = {"ai:", "query:", "admin:"})
    public void plazaInsert(PlazaManageDTO dto) {
        aiPlazaDao.insert(toPlazaPO(dto));
    }

    @Override
    @CacheEvict(keyPrefix = {"ai:", "query:", "admin:"})
    public void plazaUpdate(PlazaManageDTO dto) {
        AiPlaza existed = aiPlazaDao.queryByPlazaId(dto.getPlazaId());
        if (existed == null) {
            return;
        }
        AiPlaza po = toPlazaPO(dto);
        po.setId(existed.getId());
        aiPlazaDao.update(po);
    }

    @Override
    @CacheEvict(keyPrefix = {"ai:", "query:", "admin:"})
    public void plazaDelete(String plazaId) {
        aiPlazaCommentDao.deleteByPlazaId(plazaId);
        aiPlazaLikeDao.deleteByPlazaId(plazaId);
        aiPlazaFavorDao.deleteByPlazaId(plazaId);
        aiPlazaDao.deleteByPlazaId(plazaId);
    }

    // -------------------- Depend --------------------
    @Override
    @Cacheable(cachePrefix = ADMIN_DEPEND_PREFIX, cacheType = CacheType.LIST, cacheClass = String.class)
    public List<String> queryClientDependOnPrompt(String promptId) {
        return aiConfigDao.queryClientIdListByConfigTypeAndValue(PROMPT.getType(), promptId);
    }

    @Override
    @Cacheable(cachePrefix = ADMIN_DEPEND_PREFIX, cacheType = CacheType.LIST, cacheClass = String.class)
    public List<String> queryClientDependOnAdvisor(String advisorId) {
        return aiConfigDao.queryClientIdListByConfigTypeAndValue(ADVISOR.getType(), advisorId);
    }

    @Override
    @Cacheable(cachePrefix = ADMIN_DEPEND_PREFIX, cacheType = CacheType.LIST, cacheClass = String.class)
    public List<String> queryClientDependOnMcp(String mcpId) {
        return aiConfigDao.queryClientIdListByConfigTypeAndValue(MCP.getType(), mcpId);
    }

    @Override
    @Cacheable(cachePrefix = ADMIN_DEPEND_PREFIX, cacheType = CacheType.LIST, cacheClass = String.class)
    public List<String> queryModelDependOnApi(String apiId) {
        return aiModelDao.queryModelIdByApiId(apiId);
    }

    @Override
    @Cacheable(cachePrefix = ADMIN_DEPEND_PREFIX, cacheType = CacheType.LIST, cacheClass = String.class)
    public List<String> queryClientDependOnModel(String modelId) {
        return aiClientDao.queryClientIdByModelId(modelId);
    }

    @Override
    @Cacheable(cachePrefix = ADMIN_DEPEND_PREFIX, cacheType = CacheType.LIST, cacheClass = String.class)
    public List<String> queryAgentDependOnClient(String clientId) {
        return aiFlowDao.queryAgentIdByClientId(clientId);
    }

    @Override
    @Cacheable(cachePrefix = ADMIN_DEPEND_PREFIX, cacheType = CacheType.LIST, cacheClass = String.class)
    public List<String> queryPlazaDependOnTemplate(String templateId) {
        List<AiPlaza> aiPlazaList = aiPlazaDao.listByTemplateId(templateId);
        if (CollectionUtils.isEmpty(aiPlazaList)) {
            return List.of();
        }
        return aiPlazaList.stream().map(AiPlaza::getPlazaId).toList();
    }

    @Override
    @Cacheable(cachePrefix = ADMIN_LIST_PREFIX, cacheType = CacheType.LIST, cacheClass = String.class)
    public List<String> listApiId() {
        return aiApiDao.listApiId();
    }

    @Override
    @Cacheable(cachePrefix = ADMIN_LIST_PREFIX, cacheType = CacheType.LIST, cacheClass = String.class)
    public List<String> listModelId() {
        return aiModelDao.listModelId();
    }

    // -------------------- Util --------------------
    private String resolveUserName(Long userId) {
        if (userId == null) {
            return null;
        }
        if (userId == 0L) {
            return "system";
        }
        return aiUserDao.queryUserNameById(userId);
    }

    private ApiVO toApiVO(AiApi po) {
        if (po == null) {
            return null;
        }
        return ApiVO.builder()
                .apiId(po.getApiId())
                .apiBaseUrl(po.getApiBaseUrl())
                .apiKey(po.getApiKey())
                .apiCompletionsPath(po.getApiCompletionsPath())
                .apiEmbeddingsPath(po.getApiEmbeddingsPath())
                .apiFrom(po.getApiFrom())
                .userName(resolveUserName(po.getApiFrom()))
                .createTime(po.getCreateTime())
                .updateTime(po.getUpdateTime())
                .build();
    }

    private AiApi toApiPO(ApiManageDTO dto) {
        return AiApi.builder()
                .apiId(dto.getApiId())
                .apiBaseUrl(dto.getApiBaseUrl())
                .apiKey(dto.getApiKey())
                .apiCompletionsPath(dto.getApiCompletionsPath())
                .apiEmbeddingsPath(dto.getApiEmbeddingsPath())
                .apiFrom(0L)
                .build();
    }

    private AiModel toModelPo(ModelManageDTO dto) {
        return AiModel.builder()
                .modelId(dto.getModelId())
                .apiId(dto.getApiId())
                .modelName(dto.getModelName())
                .modelType(dto.getModelType())
                .modelFrom(0L)
                .build();
    }

    private ModelVO toModelVO(AiModel po) {
        if (po == null) {
            return null;
        }
        return ModelVO.builder()
                .modelId(po.getModelId())
                .apiId(po.getApiId())
                .modelName(po.getModelName())
                .modelType(po.getModelType())
                .modelFrom(po.getModelFrom())
                .userName(resolveUserName(po.getModelFrom()))
                .createTime(po.getCreateTime())
                .updateTime(po.getUpdateTime())
                .build();
    }

    private McpVO toMcpVO(AiMcp po) {
        if (po == null) {
            return null;
        }
        return McpVO.builder()
                .mcpId(po.getMcpId())
                .mcpName(po.getMcpName())
                .mcpType(po.getMcpType())
                .mcpParam(po.getMcpParam())
                .mcpSecret(po.getMcpSecret())
                .mcpDesc(po.getMcpDesc())
                .mcpTimeout(po.getMcpTimeout())
                .mcpChat(po.getMcpChat())
                .mcpFrom(po.getMcpFrom())
                .userName(resolveUserName(po.getMcpFrom()))
                .createTime(po.getCreateTime())
                .updateTime(po.getUpdateTime())
                .build();
    }

    private AiMcp toMcpPo(McpManageDTO dto) {
        return AiMcp.builder()
                .mcpId(dto.getMcpId())
                .mcpName(dto.getMcpName())
                .mcpType(dto.getMcpType())
                .mcpParam(dto.getMcpParam())
                .mcpSecret(dto.getMcpSecret())
                .mcpDesc(dto.getMcpDesc())
                .mcpTimeout(dto.getMcpTimeout())
                .mcpChat(dto.getMcpChat())
                .mcpFrom(0L)
                .build();
    }

    private AdvisorVO toAdvisorVO(AiAdvisor po) {
        if (po == null) {
            return null;
        }
        return AdvisorVO.builder()
                .advisorId(po.getAdvisorId())
                .advisorName(po.getAdvisorName())
                .advisorType(po.getAdvisorType())
                .advisorParam(po.getAdvisorParam())
                .updateTime(po.getUpdateTime())
                .build();
    }

    private AiAdvisor toAdvisorPo(AdvisorManageDTO dto) {
        return AiAdvisor.builder()
                .advisorId(dto.getAdvisorId())
                .advisorName(dto.getAdvisorName())
                .advisorType(dto.getAdvisorType())
                .advisorParam(dto.getAdvisorParam())
                .build();
    }

    private PromptVO toPromptVO(AiPrompt po) {
        if (po == null) {
            return null;
        }
        return PromptVO.builder()
                .promptId(po.getPromptId())
                .promptName(po.getPromptName())
                .systemPrompt(po.getSystenPrompt())
                .updateTime(po.getUpdateTime())
                .build();
    }

    private AiPrompt toPromptPo(PromptManageDTO dto) {
        return AiPrompt.builder()
                .promptId(dto.getPromptId())
                .promptName(dto.getPromptName())
                .systenPrompt(dto.getSystenPrompt())
                .build();
    }

    private ClientVO toClientVO(AiClient po) {
        if (po == null) {
            return null;
        }
        return ClientVO.builder()
                .clientId(po.getClientId())
                .modelId(po.getModelId())
                .modelName(po.getModelName())
                .clientName(po.getClientName())
                .clientStatus(po.getClientStatus())
                .clientFrom(po.getClientFrom())
                .userName(resolveUserName(po.getClientFrom()))
                .createTime(po.getCreateTime())
                .updateTime(po.getUpdateTime())
                .build();
    }

    private AiClient toClientPo(ClientManageDTO dto) {
        return AiClient.builder()
                .clientId(dto.getClientId())
                .clientType(dto.getClientType())
                .clientRole(dto.getClientRole())
                .modelId(dto.getModelId())
                .modelName(dto.getModelName())
                .clientName(dto.getClientName())
                .clientStatus(dto.getClientStatus())
                .clientFrom(0L)
                .build();
    }

    private AgentVO toAgentVO(AiAgent po) {
        if (po == null) {
            return null;
        }
        AiModel aiModel = aiModelDao.queryByModelId(po.getModelId());
        return AgentVO.builder()
                .agentId(po.getAgentId())
                .agentName(po.getAgentName())
                .agentType(po.getAgentType())
                .agentDesc(po.getAgentDesc())
                .modelId(po.getModelId())
                .modelName(aiModel.getModelName())
                .templateId(po.getTemplateId())
                .agentStatus(po.getAgentStatus())
                .agentFrom(po.getAgentFrom())
                .userName(resolveUserName(po.getAgentFrom()))
                .createTime(po.getCreateTime())
                .updateTime(po.getUpdateTime())
                .build();
    }

    private AiAgent toAgentPo(AgentManageDTO dto) {
        return AiAgent.builder()
                .agentId(dto.getAgentId())
                .agentName(dto.getAgentName())
                .agentType(dto.getAgentType())
                .agentDesc(dto.getAgentDesc())
                .modelId(dto.getModelId())
                .templateId(dto.getTemplateId())
                .agentStatus(dto.getAgentStatus())
                .agentFrom(0L)
                .build();
    }

    private UserVO toUserVO(AiUser po) {
        if (po == null) {
            return null;
        }
        return UserVO.builder()
                .userId(po.getId())
                .userName(po.getUserName())
                .userRole(po.getUserRole())
                .userAvatar(po.getUserAvatar())
                .userStatus(po.getUserStatus())
                .updateTime(po.getUpdateTime())
                .build();
    }

    private AiUser toUserPo(UserManageDTO dto) {
        return AiUser.builder()
                .userName(dto.getUserName())
                .password(dto.getPassword())
                .userRole(dto.getUserRole())
                .userAvatar(dto.getUserAvatar())
                .userStatus(dto.getUserStatus())
                .build();
    }

    private AiConfig toConfigPO(ConfigManageDTO dto) {
        return AiConfig.builder()
                .clientId(dto.getClientId())
                .configType(dto.getConfigType())
                .configValue(dto.getConfigValue())
                .configStatus(dto.getConfigStatus())
                .build();
    }

    private ConfigVO toConfigVO(AiConfig po) {
        if (po == null) {
            return null;
        }
        return ConfigVO.builder()
                .clientId(po.getClientId())
                .configType(po.getConfigType())
                .configValue(po.getConfigValue())
                .configStatus(po.getConfigStatus())
                .createTime(po.getCreateTime())
                .updateTime(po.getUpdateTime())
                .build();
    }

    private FlowVO toFlowVO(AiFlow po) {
        if (po == null) {
            return null;
        }
        return FlowVO.builder()
                .agentId(po.getAgentId())
                .clientId(po.getClientId())
                .clientRole(po.getClientRole())
                .userPrompt(po.getUserPrompt())
                .flowSeq(po.getFlowSeq())
                .updateTime(po.getUpdateTime())
                .build();
    }

    private AiFlow toFlowPO(FlowManageDTO dto) {
        return AiFlow.builder()
                .agentId(dto.getAgentId())
                .clientId(dto.getClientId())
                .clientRole(dto.getClientRole())
                .userPrompt(dto.getUserPrompt())
                .flowSeq(dto.getFlowSeq())
                .build();
    }

    private TaskVO toTaskVO(AiTask po) {
        if (po == null) {
            return null;
        }
        return TaskVO.builder()
                .taskId(po.getTaskId())
                .agentId(po.getAgentId())
                .taskCron(po.getTaskCron())
                .taskDesc(po.getTaskDesc())
                .taskParam(po.getTaskParam())
                .taskStatus(po.getTaskStatus())
                .taskFrom(po.getTaskFrom())
                .userName(resolveUserName(po.getTaskFrom()))
                .createTime(po.getCreateTime())
                .updateTime(po.getUpdateTime())
                .build();
    }

    private AiTask toTaskPO(TaskManageDTO dto) {
        return AiTask.builder()
                .taskId(dto.getTaskId())
                .agentId(dto.getAgentId())
                .taskCron(dto.getTaskCron())
                .taskDesc(dto.getTaskDesc())
                .taskParam(dto.getTaskParam())
                .taskStatus(dto.getTaskStatus())
                .taskFrom(0L)
                .build();
    }

    private TemplateVO toTemplateVO(AiTemplate po) {
        if (po == null) {
            return null;
        }
        return TemplateVO.builder()
                .templateId(po.getTemplateId())
                .userId(po.getUserId())
                .userName(resolveUserName(po.getUserId()))
                .agentName(po.getAgentName())
                .agentType(po.getAgentType())
                .agentDesc(po.getAgentDesc())
                .apiBaseUrl(po.getApiBaseUrl())
                .apiCompletionUrl(po.getApiCompletionUrl())
                .modelName(po.getModelName())
                .modelType(po.getModelType())
                .snapshot(po.getSnapshot())
                .createTime(po.getCreateTime())
                .updateTime(po.getUpdateTime())
                .build();
    }

    private AiTemplate toTemplatePO(TemplateManageDTO dto) {
        return AiTemplate.builder()
                .templateId(dto.getTemplateId())
                .userId(dto.getUserId())
                .agentName(dto.getAgentName())
                .agentType(dto.getAgentType())
                .agentDesc(dto.getAgentDesc())
                .apiBaseUrl(dto.getApiBaseUrl())
                .apiCompletionUrl(dto.getApiCompletionUrl())
                .modelName(dto.getModelName())
                .modelType(dto.getModelType())
                .snapshot(dto.getSnapshot())
                .build();
    }

    private PlazaVO toPlazaVO(AiPlaza po) {
        if (po == null) {
            return null;
        }
        return PlazaVO.builder()
                .plazaId(po.getPlazaId())
                .templateId(po.getTemplateId())
                .userId(po.getUserId())
                .userName(resolveUserName(po.getUserId()))
                .agentName(po.getAgentName())
                .agentType(po.getAgentType())
                .plazaTitle(po.getPlazaTitle())
                .plazaDesc(po.getPlazaDesc())
                .likeCount(po.getLikeCount())
                .favorCount(po.getFavorCount())
                .commentCount(po.getCommentCount())
                .createTime(po.getCreateTime())
                .updateTime(po.getUpdateTime())
                .build();
    }

    private AiPlaza toPlazaPO(PlazaManageDTO dto) {
        return AiPlaza.builder()
                .plazaId(dto.getPlazaId())
                .templateId(dto.getTemplateId())
                .userId(dto.getUserId())
                .agentName(dto.getAgentName())
                .agentType(dto.getAgentType())
                .plazaTitle(dto.getPlazaTitle())
                .plazaDesc(dto.getPlazaDesc())
                .likeCount(dto.getLikeCount())
                .favorCount(dto.getFavorCount())
                .commentCount(dto.getCommentCount())
                .build();
    }

    private SessionVO toSessionVO(AiSession po) {
        if (po == null) {
            return null;
        }
        AiUser user = aiUserDao.queryById(po.getUserId());
        return SessionVO.builder()
                .sessionId(po.getSessionId())
                .userName(user == null ? null : user.getUserName())
                .sessionTitle(po.getSessionTitle())
                .sessionType(po.getSessionType())
                .createTime(po.getCreateTime())
                .build();
    }

}
