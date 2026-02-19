package com.dasi.domain.user.repository;

import com.dasi.domain.user.model.vo.UserVO;

public interface IUserRepository {

    UserVO queryByUsername(String username);

    UserVO queryById(Long id);

    UserVO insertUser(String username, String password);

    UserVO updateUser(Long id, String username, String password);

}
