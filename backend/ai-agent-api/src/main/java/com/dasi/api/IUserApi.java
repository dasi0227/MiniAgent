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

    Result<AuthVO> login(AuthDTO request);

    Result<AuthVO> register(AuthDTO request);

    Result<AuthVO> profileQuery();

    Result<AuthVO> profileEdit(ProfileEditDTO request, MultipartFile avatar);

    Result<List<UserApiVO>> apiList(String keyword);

    Result<Void> apiInsert(SettingApiDTO request);

    Result<Void> apiUpdate(SettingApiDTO request);

    Result<Void> apiDelete(Long id);

    Result<List<UserMcpVO>> mcpList(String keyword);

    Result<Void> mcpInsert(SettingMcpDTO request);

    Result<Void> mcpUpdate(SettingMcpDTO request);

    Result<Void> mcpDelete(Long id);
}
