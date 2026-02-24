package com.dasi.domain.user.service.setting;

import com.dasi.domain.user.model.vo.AuthVO;
import com.dasi.domain.user.model.vo.UserApiVO;
import com.dasi.domain.user.model.vo.UserMcpVO;
import com.dasi.types.dto.request.user.ProfileEditRequest;
import com.dasi.types.dto.request.user.SettingApiRequest;
import com.dasi.types.dto.request.user.SettingMcpRequest;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ISettingService {

    AuthVO profileQuery();

    AuthVO profileEdit(ProfileEditRequest request, MultipartFile avatar);

    List<UserApiVO> apiList(String keyword);

    void apiInsert(SettingApiRequest request);

    void apiUpdate(SettingApiRequest request);

    void apiDelete(Long id);

    List<UserMcpVO> mcpList(String keyword);

    void mcpInsert(SettingMcpRequest request);

    void mcpUpdate(SettingMcpRequest request);

    void mcpDelete(Long id);

}
