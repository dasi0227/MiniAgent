package com.dasi.domain.user.repository;

import com.dasi.domain.user.model.vo.UserApiVO;
import com.dasi.domain.user.model.vo.UserMcpVO;
import com.dasi.domain.user.model.vo.UserVO;
import com.dasi.domain.user.model.dto.SettingApiDTO;
import com.dasi.domain.user.model.dto.SettingMcpDTO;

import java.util.List;

public interface IUserRepository {

    UserVO queryUserByUserName(String userName);

    UserVO queryUserById(Long id);

    UserVO insertUser(String userName, String password);

    UserVO updateUser(Long id, String userName, String password, String userAvatar);

    List<UserApiVO> apiList(String keyword);

    void apiInsert(SettingApiDTO request, String apiId, String modelId);

    void apiUpdate(SettingApiDTO request);

    void apiDelete(Long id);

    List<UserMcpVO> mcpList(String keyword);

    void mcpInsert(SettingMcpDTO request, String mcpId);

    void mcpUpdate(SettingMcpDTO request);

    void mcpDelete(Long id);

}
