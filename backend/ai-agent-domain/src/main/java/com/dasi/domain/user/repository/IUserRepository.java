package com.dasi.domain.user.repository;

import com.dasi.domain.user.model.vo.UserApiVO;
import com.dasi.domain.user.model.vo.UserVO;
import com.dasi.types.dto.request.user.SettingApiRequest;

import java.util.List;

public interface IUserRepository {

    UserVO queryUserByUserName(String userName);

    UserVO queryUserById(Long id);

    UserVO insertUser(String userName, String password);

    UserVO updateUser(Long id, String userName, String password, String userAvatar);

    List<UserApiVO> apiList(String keyword);

    void apiInsert(SettingApiRequest request);

    void apiUpdate(SettingApiRequest request);

    void apiDelete(Long id);

}
