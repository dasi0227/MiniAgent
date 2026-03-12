package com.dasi.domain.user.service.setting;

import com.dasi.domain.user.model.vo.AuthVO;
import com.dasi.domain.user.model.vo.UserApiModelVO;
import com.dasi.domain.user.model.vo.UserMcpVO;
import com.dasi.domain.user.model.vo.UserTaskVO;
import com.dasi.domain.user.model.dto.ProfileEditDTO;
import com.dasi.domain.user.model.dto.SettingApiModelDTO;
import com.dasi.domain.user.model.dto.SettingMcpDTO;
import com.dasi.domain.user.model.dto.SettingTaskDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ISettingService {

    AuthVO profileQuery();

    AuthVO profileEdit(ProfileEditDTO dto, MultipartFile avatar);

    List<UserApiModelVO> apiModelList(String keyword);

    void apiModelInsert(SettingApiModelDTO dto);

    void apiModelUpdate(SettingApiModelDTO dto);

    void apiModelDelete(String apiId);

    List<UserMcpVO> mcpList(String keyword);

    void mcpInsert(SettingMcpDTO dto);

    void mcpUpdate(SettingMcpDTO dto);

    void mcpDelete(String mcpId);

    List<UserTaskVO> taskList();

    void taskInsert(SettingTaskDTO dto);

    void taskUpdate(SettingTaskDTO dto);

    void taskDelete(String taskId);

    void taskToggle(String taskId, Integer taskStatus);

}
