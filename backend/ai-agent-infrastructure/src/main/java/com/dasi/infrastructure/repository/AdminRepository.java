package com.dasi.infrastructure.repository;

import com.dasi.domain.admin.model.dto.*;
import com.dasi.domain.admin.model.enumeration.AiConfigType;
import com.dasi.domain.admin.model.vo.*;
import com.dasi.domain.admin.repository.IAdminRepository;
import com.dasi.domain.session.model.vo.SessionVO;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
    private IAiUserDao userDao;

    @Resource
    private IAiFlowDao aiFlowDao;

    @Resource
    private IAiTaskDao aiTaskDao;

    @Resource
    private IAiSessionDao sessionDao;

    @Resource
    private IAiMessageDao messageDao;

    @Resource
    private IAiStatDao aiStatDao;

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
                .userCount(safeInt(userDao.countAll()))
                .sessionCount(safeInt(sessionDao.countAll()))
                .messageCount(safeInt(messageDao.countAll()))
                .taskCount(safeInt(aiTaskDao.countAll()))
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
                .workCount(safeInt(sessionDao.countByType(STAT_WORK)))
                .chatCount(safeInt(sessionDao.countByType(STAT_CHAT)))
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

        Map<String, Integer> countMap = messageDao.countByDateRange(start, end)
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

        String idKeyword = apiPageDTO.getIdKeyword();
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
        return aiApiDao.count(apiPageDTO.getIdKeyword());
    }

    @Override
    @Cacheable(cachePrefix = ADMIN_API_PREFIX, cacheClass = ApiVO.class, cacheType = CacheType.VALUE)
    public ApiVO apiQuery(Long id) {
        AiApi aiApi = aiApiDao.queryById(id);
        return toApiVO(aiApi);
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
        AiApi aiApi = toApiPO(apiManageDTO);
        aiApiDao.update(aiApi);
    }

    @Override
    @CacheEvict(keyPrefix = {"ai:", "query:", "admin:"})
    public void apiDelete(Long id) {
        aiApiDao.deleteById(id);
    }

    // -------------------- Model --------------------
    @Override
    @Cacheable(cachePrefix = ADMIN_MODEL_PREFIX, cacheClass = ModelVO.class, cacheType = CacheType.LIST)
    public List<ModelVO> modelPage(ModelPageDTO dto) {

        String idKeyword = dto.getIdKeyword();
        String nameKeyword = dto.getNameKeyword();
        String apiId = dto.getApiId();
        Integer pageNum = dto.getPageNum();
        Integer pageSize = dto.getPageSize();
        Integer offset = (pageNum - 1) * pageSize;

        List<AiModel> poList = aiModelDao.page(idKeyword, nameKeyword, apiId, offset, pageSize);
        if (CollectionUtils.isEmpty(poList)) {
            return List.of();
        }

        return poList.stream().map(this::toModelVO).toList();
    }

    @Override
    @Cacheable(cachePrefix = ADMIN_MODEL_PREFIX, cacheClass = Integer.class, cacheType = CacheType.VALUE)
    public Integer modelCount(ModelPageDTO dto) {
        return aiModelDao.count(dto.getIdKeyword(), dto.getNameKeyword());
    }

    @Override
    @Cacheable(cachePrefix = ADMIN_MODEL_PREFIX, cacheClass = ModelVO.class, cacheType = CacheType.VALUE)
    public ModelVO modelQuery(Long id) {
        AiModel po = aiModelDao.queryById(id);
        return toModelVO(po);
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
        aiModelDao.update(toModelPo(dto));
    }

    @Override
    @CacheEvict(keyPrefix = {"ai:", "query:", "admin:"})
    public void modelDelete(Long id) {
        aiModelDao.deleteById(id);
    }

    // -------------------- MCP --------------------
    @Override
    @Cacheable(cachePrefix = ADMIN_MCP_PREFIX, cacheClass = McpVO.class, cacheType = CacheType.LIST)
    public List<McpVO> mcpPage(McpPageDTO dto) {
        Integer offset = (dto.getPageNum() - 1) * dto.getPageSize();
        List<AiMcp> poList = aiMcpDao.page(dto.getIdKeyword(), dto.getNameKeyword(), offset, dto.getPageSize());
        if (CollectionUtils.isEmpty(poList)) {
            return List.of();
        }
        return poList.stream().map(this::toMcpVO).toList();
    }

    @Override
    @Cacheable(cachePrefix = ADMIN_MCP_PREFIX, cacheClass = Integer.class, cacheType = CacheType.VALUE)
    public Integer mcpCount(McpPageDTO dto) {
        return aiMcpDao.count(dto.getIdKeyword(), dto.getNameKeyword());
    }

    @Override
    @Cacheable(cachePrefix = ADMIN_MCP_PREFIX, cacheClass = McpVO.class, cacheType = CacheType.VALUE)
    public McpVO mcpQuery(Long id) {
        return toMcpVO(aiMcpDao.queryById(id));
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
        aiMcpDao.update(toMcpPo(dto));
    }

    @Override
    @CacheEvict(keyPrefix = {"ai:", "query:", "admin:"})
    public void mcpDelete(Long id) {
        aiMcpDao.delete(id);
    }

    // -------------------- Advisor --------------------
    @Override
    @Cacheable(cachePrefix = ADMIN_ADVISOR_PREFIX, cacheClass = AdvisorVO.class, cacheType = CacheType.LIST)
    public List<AdvisorVO> advisorPage(AdvisorPageDTO dto) {
        Integer offset = (dto.getPageNum() - 1) * dto.getPageSize();
        List<AiAdvisor> poList = aiAdvisorDao.page(dto.getIdKeyword(), dto.getNameKeyword(), offset, dto.getPageSize());
        if (CollectionUtils.isEmpty(poList)) {
            return List.of();
        }
        return poList.stream().map(this::toAdvisorVO).toList();
    }

    @Override
    @Cacheable(cachePrefix = ADMIN_ADVISOR_PREFIX, cacheClass = Integer.class, cacheType = CacheType.VALUE)
    public Integer advisorCount(AdvisorPageDTO dto) {
        return aiAdvisorDao.count(dto.getIdKeyword(), dto.getNameKeyword());
    }

    @Override
    @Cacheable(cachePrefix = ADMIN_ADVISOR_PREFIX, cacheClass = AdvisorVO.class, cacheType = CacheType.VALUE)
    public AdvisorVO advisorQuery(Long id) {
        return toAdvisorVO(aiAdvisorDao.queryById(id));
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
        aiAdvisorDao.update(toAdvisorPo(dto));
    }

    @Override
    @CacheEvict(keyPrefix = {"ai:", "query:", "admin:"})
    public void advisorDelete(Long id) {
        aiAdvisorDao.delete(id);
    }

    // -------------------- Prompt --------------------
    @Override
    @Cacheable(cachePrefix = ADMIN_PROMPT_PREFIX, cacheClass = PromptVO.class, cacheType = CacheType.LIST)
    public List<PromptVO> promptPage(PromptPageDTO dto) {
        Integer offset = (dto.getPageNum() - 1) * dto.getPageSize();
        List<AiPrompt> poList = aiPromptDao.page(dto.getIdKeyword(), dto.getNameKeyword(), offset, dto.getPageSize());
        if (CollectionUtils.isEmpty(poList)) {
            return List.of();
        }
        return poList.stream().map(this::toPromptVO).toList();
    }

    @Override
    @Cacheable(cachePrefix = ADMIN_PROMPT_PREFIX, cacheClass = Integer.class, cacheType = CacheType.VALUE)
    public Integer promptCount(PromptPageDTO dto) {
        return aiPromptDao.count(dto.getIdKeyword(), dto.getNameKeyword());
    }

    @Override
    @Cacheable(cachePrefix = ADMIN_PROMPT_PREFIX, cacheClass = PromptVO.class, cacheType = CacheType.VALUE)
    public PromptVO promptQuery(Long id) {
        return toPromptVO(aiPromptDao.queryById(id));
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
        aiPromptDao.update(toPromptPo(dto));
    }

    @Override
    @CacheEvict(keyPrefix = {"ai:", "query:", "admin:"})
    public void promptDelete(Long id) {
        aiPromptDao.delete(id);
    }

    // -------------------- Client --------------------
    @Override
    @Cacheable(cachePrefix = ADMIN_CLIENT_PREFIX, cacheClass = ClientVO.class, cacheType = CacheType.LIST)
    public List<ClientVO> clientPage(ClientPageDTO dto) {
        Integer offset = (dto.getPageNum() - 1) * dto.getPageSize();
        List<AiClient> poList = aiClientDao.page(dto.getIdKeyword(), dto.getNameKeyword(), dto.getModelId(), dto.getClientType(), dto.getClientRole(), offset, dto.getPageSize());
        if (CollectionUtils.isEmpty(poList)) {
            return List.of();
        }
        return poList.stream().map(this::toClientVO).toList();
    }

    @Override
    @Cacheable(cachePrefix = ADMIN_CLIENT_PREFIX, cacheClass = Integer.class, cacheType = CacheType.VALUE)
    public Integer clientCount(ClientPageDTO dto) {
        return aiClientDao.count(dto.getIdKeyword(), dto.getNameKeyword(), dto.getModelId(), dto.getClientType(), dto.getClientRole());
    }

    @Override
    @Cacheable(cachePrefix = ADMIN_CLIENT_PREFIX, cacheClass = ClientVO.class, cacheType = CacheType.VALUE)
    public ClientVO clientQuery(Long id) {
        return toClientVO(aiClientDao.queryById(id));
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
        aiClientDao.update(toClientPo(dto));
    }

    @Override
    @CacheEvict(keyPrefix = {"ai:", "query:", "admin:"})
    public void clientDelete(Long id) {
        aiClientDao.delete(id);
    }

    @Override
    @CacheEvict(keyPrefix = {"ai:", "query:", "admin:"})
    public void clientToggle(Long id, Integer status) {
        AiClient po = AiClient.builder()
                .id(id)
                .clientStatus(status)
                .build();
        aiClientDao.toggle(po);
    }

    // -------------------- Agent --------------------
    @Override
    @Cacheable(cachePrefix = ADMIN_AGENT_PREFIX, cacheClass = AgentVO.class, cacheType = CacheType.LIST)
    public List<AgentVO> agentPage(AgentPageDTO dto) {
        Integer offset = (dto.getPageNum() - 1) * dto.getPageSize();
        List<AiAgent> poList = aiAgentDao.page(dto.getIdKeyword(), dto.getNameKeyword(), dto.getAgentType(), offset, dto.getPageSize());
        if (CollectionUtils.isEmpty(poList)) {
            return List.of();
        }
        return poList.stream().map(this::toAgentVO).toList();
    }

    @Override
    @Cacheable(cachePrefix = ADMIN_AGENT_PREFIX, cacheClass = AgentVO.class, cacheType = CacheType.LIST)
    public List<AgentVO> agentList(AgentListDTO dto) {
        List<AiAgent> poList = aiAgentDao.list(dto.getIdKeyword(), dto.getNameKeyword(), dto.getAgentType());
        return poList.stream().map(this::toAgentVO).toList();
    }

    @Override
    @Cacheable(cachePrefix = ADMIN_AGENT_PREFIX, cacheClass = Integer.class, cacheType = CacheType.VALUE)
    public Integer agentCount(AgentPageDTO dto) {
        return aiAgentDao.count(dto.getIdKeyword(), dto.getNameKeyword(), dto.getAgentType());
    }

    @Override
    @Cacheable(cachePrefix = ADMIN_AGENT_PREFIX, cacheClass = AgentVO.class, cacheType = CacheType.VALUE)
    public AgentVO agentQuery(Long id) {
        return toAgentVO(aiAgentDao.queryById(id));
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
        aiAgentDao.update(toAgentPo(dto));
    }

    @Override
    @CacheEvict(keyPrefix = {"ai:", "query:", "admin:"})
    public void agentDelete(Long id) {
        aiAgentDao.delete(id);
    }

    @Override
    @CacheEvict(keyPrefix = {"ai:", "query:", "admin:"})
    public void agentToggle(Long id, Integer status) {
        AiAgent po = AiAgent.builder()
                .id(id)
                .agentStatus(status)
                .build();
        aiAgentDao.toggle(po);
    }

    // -------------------- AiUser --------------------
    @Override
    @Cacheable(cachePrefix = ADMIN_USER_PREFIX, cacheClass = UserVO.class, cacheType = CacheType.LIST)
    public List<UserVO> userPage(UserPageDTO dto) {
        Integer offset = (dto.getPageNum() - 1) * dto.getPageSize();
        List<AiUser> poList = userDao.page(dto.getUserNameKeyWord(), dto.getUserRole(), offset, dto.getPageSize());
        if (CollectionUtils.isEmpty(poList)) {
            return List.of();
        }
        return poList.stream().map(this::toUserVO).toList();
    }

    @Override
    @Cacheable(cachePrefix = ADMIN_USER_PREFIX, cacheClass = Integer.class, cacheType = CacheType.VALUE)
    public Integer userCount(UserPageDTO dto) {
        Long count = userDao.count(dto.getUserNameKeyWord(), dto.getUserRole());
        return count == null ? 0 : count.intValue();
    }

    @Override
    @Cacheable(cachePrefix = ADMIN_USER_PREFIX, cacheClass = UserVO.class, cacheType = CacheType.VALUE)
    public UserVO userQuery(Long id) {
        return toUserVO(userDao.queryById(id));
    }

    @Override
    @Cacheable(cachePrefix = ADMIN_USER_PREFIX, cacheClass = UserVO.class, cacheType = CacheType.VALUE)
    public UserVO userQuery(String userName) {
        return toUserVO(userDao.queryByUserName(userName));
    }

    @Override
    @CacheEvict(keyPrefix = {"ai:", "query:", "admin:"})
    public void userInsert(UserManageDTO dto) {
        userDao.insert(toUserPo(dto));
    }

    @Override
    @CacheEvict(keyPrefix = {"ai:", "query:", "admin:"})
    public void userUpdate(UserManageDTO dto) {
        userDao.update(toUserPo(dto));
    }

    @Override
    @CacheEvict(keyPrefix = {"ai:", "query:", "admin:"})
    public void userDelete(Long id) {
        userDao.delete(id);
    }

    @Override
    @CacheEvict(keyPrefix = {"ai:", "query:", "admin:"})
    public void userToggle(Long id, Integer status) {
        userDao.toggle(id, status);
    }

    // -------------------- Config --------------------
    @Override
    @Cacheable(cachePrefix = ADMIN_CONFIG_PREFIX, cacheType = CacheType.LIST, cacheClass = ConfigVO.class)
    public List<ConfigVO> configList(ConfigListDTO dto) {
        List<AiConfig> poList = aiConfigDao.list(dto.getIdKeyword(), dto.getValueKeyword(), dto.getConfigType());
        return poList.stream().map(this::toConfigVO).toList();
    }

    @Override
    @Cacheable(cachePrefix = ADMIN_CONFIG_PREFIX, cacheType = CacheType.VALUE, cacheClass = ConfigVO.class)
    public ConfigVO configQuery(ConfigManageDTO dto) {
        return toConfigVO(aiConfigDao.queryByUniqueKey(dto.getClientId(), dto.getConfigType(), dto.getConfigValue()));
    }

    @Override
    @Cacheable(cachePrefix = ADMIN_CONFIG_PREFIX, cacheType = CacheType.VALUE, cacheClass = ConfigVO.class)
    public ConfigVO configQuery(Long id) {
        return toConfigVO(aiConfigDao.queryById(id));
    }

    @Override
    @CacheEvict(keyPrefix = {"ai:", "query:", "admin:"})
    public void configInsert(ConfigManageDTO dto) {
        aiConfigDao.insert(toConfigPO(dto));
    }

    @Override
    @CacheEvict(keyPrefix = {"ai:", "query:", "admin:"})
    public void configUpdate(ConfigManageDTO dto) {
        aiConfigDao.update(toConfigPO(dto));
    }

    @Override
    @CacheEvict(keyPrefix = {"ai:", "query:", "admin:"})
    public void configDelete(Long id) {
        aiConfigDao.delete(id);
    }

    @Override
    @CacheEvict(keyPrefix = {"ai:", "query:", "admin:"})
    public void configToggle(Long id, Integer status) {
        aiConfigDao.toggle(id, status);
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
    @Cacheable(cachePrefix = ADMIN_FLOW_PREFIX, cacheType = CacheType.VALUE, cacheClass = FlowVO.class)
    public FlowVO flowQuery(Long id) {
        return toFlowVO(aiFlowDao.queryById(id));
    }

    @Override
    @CacheEvict(keyPrefix = {"ai:", "query:", "admin:"})
    public void flowInsert(FlowManageDTO dto) {
        aiFlowDao.insert(toFlowPO(dto));
    }

    @Override
    @CacheEvict(keyPrefix = {"ai:", "query:", "admin:"})
    public void flowUpdate(FlowManageDTO dto) {
        aiFlowDao.update(toFlowPO(dto));
    }

    @Override
    @CacheEvict(keyPrefix = {"ai:", "query:", "admin:"})
    public void flowDelete(Long id) {
        aiFlowDao.delete(id);
    }

    // -------------------- Task --------------------
    @Override
    @Cacheable(cachePrefix = ADMIN_TASK_PREFIX, cacheType = CacheType.LIST, cacheClass = TaskVO.class)
    public List<TaskVO> taskPage(TaskPageDTO dto) {
        Integer offset = (dto.getPageNum() - 1) * dto.getPageSize();
        List<AiTask> poList = aiTaskDao.page(dto.getIdKeyword(), dto.getAgentId(), offset, dto.getPageSize());
        if (CollectionUtils.isEmpty(poList)) {
            return List.of();
        }
        return poList.stream().map(this::toTaskVO).toList();
    }

    @Override
    @Cacheable(cachePrefix = ADMIN_TASK_PREFIX, cacheType = CacheType.VALUE, cacheClass = Integer.class)
    public Integer taskCount(TaskPageDTO dto) {
        return aiTaskDao.count(dto.getIdKeyword(), dto.getAgentId());
    }

    @Override
    @Cacheable(cachePrefix = ADMIN_TASK_PREFIX, cacheType = CacheType.VALUE, cacheClass = TaskVO.class)
    public TaskVO taskQuery(Long id) {
        return toTaskVO(aiTaskDao.queryById(id));
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
        aiTaskDao.update(toTaskPO(dto));
    }

    @Override
    @CacheEvict(keyPrefix = {"ai:", "query:", "admin:"})
    public void taskDelete(Long id) {
        aiTaskDao.delete(id);
    }

    @Override
    @CacheEvict(keyPrefix = {"ai:", "query:", "admin:"})
    public void taskToggle(Long id, Integer status) {
        AiTask po = AiTask.builder()
                .id(id)
                .taskStatus(status)
                .build();
        aiTaskDao.toggle(po);
    }

    // -------------------- AiSession --------------------
    @Override
    public List<SessionVO> listSession() {
        List<AiSession> list = sessionDao.queryAll();
        return list.stream().map(this::toSessionVO).toList();
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
    private ApiVO toApiVO(AiApi po) {
        if (po == null) {
            return null;
        }
        return ApiVO.builder()
                .id(po.getId())
                .apiId(po.getApiId())
                .apiBaseUrl(po.getApiBaseUrl())
                .apiKey(po.getApiKey())
                .apiCompletionsPath(po.getApiCompletionsPath())
                .apiEmbeddingsPath(po.getApiEmbeddingsPath())
                .updateTime(po.getUpdateTime())
                .build();
    }

    private AiApi toApiPO(ApiManageDTO dto) {
        return AiApi.builder()
                .id(dto.getId())
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
                .id(dto.getId())
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
                .id(po.getId())
                .modelId(po.getModelId())
                .apiId(po.getApiId())
                .modelName(po.getModelName())
                .modelType(po.getModelType())
                .updateTime(po.getUpdateTime())
                .build();
    }

    private McpVO toMcpVO(AiMcp po) {
        if (po == null) {
            return null;
        }
        return McpVO.builder()
                .id(po.getId())
                .mcpId(po.getMcpId())
                .mcpName(po.getMcpName())
                .mcpType(po.getMcpType())
                .mcpParam(po.getMcpParam())
                .mcpDesc(po.getMcpDesc())
                .mcpTimeout(po.getMcpTimeout())
                .mcpChat(po.getMcpChat())
                .updateTime(po.getUpdateTime())
                .build();
    }

    private AiMcp toMcpPo(McpManageDTO dto) {
        return AiMcp.builder()
                .id(dto.getId())
                .mcpId(dto.getMcpId())
                .mcpName(dto.getMcpName())
                .mcpType(dto.getMcpType())
                .mcpParam(dto.getMcpParam())
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
                .id(po.getId())
                .advisorId(po.getAdvisorId())
                .advisorName(po.getAdvisorName())
                .advisorType(po.getAdvisorType())
                .advisorParam(po.getAdvisorParam())
                .updateTime(po.getUpdateTime())
                .build();
    }

    private AiAdvisor toAdvisorPo(AdvisorManageDTO dto) {
        return AiAdvisor.builder()
                .id(dto.getId())
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
                .id(po.getId())
                .promptId(po.getPromptId())
                .promptName(po.getPromptName())
                .systenPrompt(po.getSystenPrompt())
                .updateTime(po.getUpdateTime())
                .build();
    }

    private AiPrompt toPromptPo(PromptManageDTO dto) {
        return AiPrompt.builder()
                .id(dto.getId())
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
                .id(po.getId())
                .clientId(po.getClientId())
                .clientType(po.getClientType())
                .clientRole(po.getClientRole())
                .modelId(po.getModelId())
                .modelName(po.getModelName())
                .clientName(po.getClientName())
                .clientStatus(po.getClientStatus())
                .updateTime(po.getUpdateTime())
                .build();
    }

    private AiClient toClientPo(ClientManageDTO dto) {
        return AiClient.builder()
                .id(dto.getId())
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
        return AgentVO.builder()
                .id(po.getId())
                .agentId(po.getAgentId())
                .agentName(po.getAgentName())
                .agentType(po.getAgentType())
                .agentDesc(po.getAgentDesc())
                .agentStatus(po.getAgentStatus())
                .updateTime(po.getUpdateTime())
                .build();
    }

    private AiAgent toAgentPo(AgentManageDTO dto) {
        return AiAgent.builder()
                .id(dto.getId())
                .agentId(dto.getAgentId())
                .agentName(dto.getAgentName())
                .agentType(dto.getAgentType())
                .agentDesc(dto.getAgentDesc())
                .agentStatus(dto.getAgentStatus())
                .agentFrom(0L)
                .build();
    }

    private UserVO toUserVO(AiUser po) {
        if (po == null) {
            return null;
        }
        return UserVO.builder()
                .id(po.getId())
                .userName(po.getUserName())
                .userRole(po.getUserRole())
                .userAvatar(po.getUserAvatar())
                .userStatus(po.getUserStatus())
                .updateTime(po.getUpdateTime())
                .build();
    }

    private AiUser toUserPo(UserManageDTO dto) {
        return AiUser.builder()
                .id(dto.getId())
                .userName(dto.getUserName())
                .password(dto.getPassword())
                .userRole(dto.getUserRole())
                .userAvatar(dto.getUserAvatar())
                .userStatus(dto.getUserStatus())
                .build();
    }

    private AiConfig toConfigPO(ConfigManageDTO dto) {
        return AiConfig.builder()
                .id(dto.getId())
                .clientId(dto.getClientId())
                .configType(dto.getConfigType())
                .configValue(dto.getConfigValue())
                .configParam(dto.getConfigParam())
                .configStatus(dto.getConfigStatus())
                .build();
    }

    private ConfigVO toConfigVO(AiConfig po) {
        if (po == null) {
            return null;
        }
        return ConfigVO.builder()
                .id(po.getId())
                .clientId(po.getClientId())
                .configType(po.getConfigType())
                .configValue(po.getConfigValue())
                .configParam(po.getConfigParam())
                .configStatus(po.getConfigStatus())
                .updateTime(po.getUpdateTime())
                .build();
    }

    private FlowVO toFlowVO(AiFlow po) {
        if (po == null) {
            return null;
        }
        return FlowVO.builder()
                .id(po.getId())
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
                .id(dto.getId())
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
                .id(po.getId())
                .taskId(po.getTaskId())
                .agentId(po.getAgentId())
                .taskCron(po.getTaskCron())
                .taskDesc(po.getTaskDesc())
                .taskParam(po.getTaskParam())
                .taskStatus(po.getTaskStatus())
                .updateTime(po.getUpdateTime())
                .build();
    }

    private AiTask toTaskPO(TaskManageDTO dto) {
        return AiTask.builder()
                .id(dto.getId())
                .taskId(dto.getTaskId())
                .agentId(dto.getAgentId())
                .taskCron(dto.getTaskCron())
                .taskDesc(dto.getTaskDesc())
                .taskParam(dto.getTaskParam())
                .taskStatus(dto.getTaskStatus())
                .build();
    }

    private SessionVO toSessionVO(AiSession session) {
        if (session == null) {
            return null;
        }
        return SessionVO.builder()
                .id(session.getId())
                .sessionId(session.getSessionId())
                .sessionUser(session.getSessionUser())
                .sessionTitle(session.getSessionTitle())
                .sessionType(session.getSessionType())
                .createTime(session.getCreateTime())
                .build();
    }

}
