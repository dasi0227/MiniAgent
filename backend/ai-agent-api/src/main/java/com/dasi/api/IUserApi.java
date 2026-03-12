package com.dasi.api;

import com.dasi.domain.user.model.vo.*;
import com.dasi.domain.user.model.dto.AuthDTO;
import com.dasi.domain.user.model.dto.ProfileEditDTO;
import com.dasi.domain.user.model.dto.SettingApiModelDTO;
import com.dasi.domain.user.model.dto.SettingMcpDTO;
import com.dasi.domain.user.model.dto.SettingTaskDTO;
import com.dasi.types.result.Result;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface IUserApi {

    Result<List<QueryChatClientVO>> queryChatClientList();

    Result<List<QueryMcpVO>> queryMcpList();

    Result<List<QueryRagVO>> queryRagList();

    Result<List<QueryWorkAgentVO>> queryWorkAgentList();

    Result<Map<String, QueryRoleVO>> queryRoleMap();

    Result<AuthVO> login(AuthDTO dto);

    Result<AuthVO> register(AuthDTO dto);

    Result<AuthVO> profileQuery();

    Result<AuthVO> profileEdit(ProfileEditDTO dto, MultipartFile avatar);

    Result<List<UserApiModelVO>> apiModelList(String keyword);

    Result<Void> apiModelInsert(SettingApiModelDTO dto);

    Result<Void> apiModelUpdate(SettingApiModelDTO dto);

    Result<Void> apiModelDelete(String apiId);

    Result<List<UserMcpVO>> mcpList(String keyword);

    Result<Void> mcpInsert(SettingMcpDTO dto);

    Result<Void> mcpUpdate(SettingMcpDTO dto);

    Result<Void> mcpDelete(String mcpId);

    Result<List<UserTaskVO>> taskList();

    Result<Void> taskInsert(SettingTaskDTO dto);

    Result<Void> taskUpdate(SettingTaskDTO dto);

    Result<Void> taskDelete(String taskId);

    Result<Void> taskToggle(String taskId, Integer taskStatus);
}
