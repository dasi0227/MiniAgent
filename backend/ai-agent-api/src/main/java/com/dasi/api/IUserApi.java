package com.dasi.api;

import com.dasi.domain.user.model.vo.*;
import com.dasi.domain.user.model.dto.AuthDTO;
import com.dasi.domain.user.model.dto.ProfileEditDTO;
import com.dasi.domain.user.model.dto.SettingApiDTO;
import com.dasi.domain.user.model.dto.SettingMcpDTO;
import com.dasi.types.result.Result;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface IUserApi {

    Result<List<ChatClientVO>> queryChatClientVOList();

    Result<List<ChatMcpVO>> queryChatMcpVOList();

    Result<List<ChatRagVO>> queryRagVOList();

    Result<List<WorkAgentVO>> queryWorkAgentVOList();

    Result<AuthVO> login(AuthDTO dto);

    Result<AuthVO> register(AuthDTO dto);

    Result<AuthVO> profileQuery();

    Result<AuthVO> profileEdit(ProfileEditDTO dto, MultipartFile avatar);

    Result<List<UserApiVO>> apiList(String keyword);

    Result<Void> apiInsert(SettingApiDTO dto);

    Result<Void> apiUpdate(SettingApiDTO dto);

    Result<Void> apiDelete(Long id);

    Result<List<UserMcpVO>> mcpList(String keyword);

    Result<Void> mcpInsert(SettingMcpDTO dto);

    Result<Void> mcpUpdate(SettingMcpDTO dto);

    Result<Void> mcpDelete(Long id);
}
