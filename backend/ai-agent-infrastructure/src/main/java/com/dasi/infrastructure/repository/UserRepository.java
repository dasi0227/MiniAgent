package com.dasi.infrastructure.repository;

import com.dasi.domain.user.model.vo.UserVO;
import com.dasi.domain.user.repository.IUserRepository;
import com.dasi.infrastructure.persistent.dao.IAiUserDao;
import com.dasi.infrastructure.persistent.po.AiUser;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Repository;

import static com.dasi.domain.user.model.enumeration.UserRoleType.ACCOUNT;

@Repository
public class UserRepository implements IUserRepository {

    @Resource
    private IAiUserDao userDao;

    @Override
    public UserVO queryByUsername(String username) {
        AiUser user = userDao.queryByUsername(username);
        return toUserVO(user);
    }

    @Override
    public UserVO queryById(Long id) {
        AiUser user = userDao.queryById(id);
        return toUserVO(user);
    }

    @Override
    public UserVO insertUser(String username, String password) {
        AiUser user = AiUser.builder()
                .username(username)
                .password(password)
                .userrole(ACCOUNT.getType())
                .userStatus(1)
                .build();
        userDao.insert(user);
        return toUserVO(user);
    }

    @Override
    public UserVO updateUser(Long id, String username, String password) {
        AiUser user = AiUser.builder()
                .id(id)
                .username(username)
                .password(password)
                .build();
        userDao.update(user);
        return toUserVO(user);
    }

    private UserVO toUserVO(AiUser user) {
        if (user == null) {
            return null;
        }
        return UserVO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .password(user.getPassword())
                .userrole(user.getUserrole())
                .userStatus(user.getUserStatus())
                .build();
    }

}
