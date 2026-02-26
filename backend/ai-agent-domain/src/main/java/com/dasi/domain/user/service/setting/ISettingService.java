package com.dasi.domain.user.service.setting;

import com.dasi.domain.user.model.vo.AuthVO;
import com.dasi.domain.user.model.vo.UserApiVO;
import com.dasi.domain.user.model.vo.UserMcpVO;
import com.dasi.domain.user.model.dto.ProfileEditDTO;
import com.dasi.domain.user.model.dto.SettingApiDTO;
import com.dasi.domain.user.model.dto.SettingMcpDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ISettingService {

    AuthVO profileQuery();

    AuthVO profileEdit(ProfileEditDTO request, MultipartFile avatar);

    List<UserApiVO> apiList(String keyword);

    void apiInsert(SettingApiDTO request);

    void apiUpdate(SettingApiDTO request);

    void apiDelete(Long id);

    List<UserMcpVO> mcpList(String keyword);

    void mcpInsert(SettingMcpDTO request);

    void mcpUpdate(SettingMcpDTO request);

    void mcpDelete(Long id);

}
