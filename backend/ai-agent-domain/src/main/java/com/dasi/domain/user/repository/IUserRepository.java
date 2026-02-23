package com.dasi.domain.user.repository;

import com.dasi.domain.user.model.vo.UserVO;

public interface IUserRepository {

    UserVO queryByUserName(String userName);

    UserVO queryById(Long id);

    UserVO insertUser(String userName, String password);

    UserVO updateUser(Long id, String userName, String password, String userAvatar);

}
