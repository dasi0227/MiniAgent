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
    public UserVO queryByUserName(String userName) {
        AiUser user = userDao.queryByUserName(userName);
        return toUserVO(user);
    }

    @Override
    public UserVO queryById(Long id) {
        AiUser user = userDao.queryById(id);
        return toUserVO(user);
    }

    @Override
    public UserVO insertUser(String userName, String password) {
        AiUser user = AiUser.builder()
                .userName(userName)
                .password(password)
                .userRole(ACCOUNT.getType())
                .userAvatar("")
                .userStatus(1)
                .build();
        userDao.insert(user);
        return toUserVO(user);
    }

    @Override
    public UserVO updateUser(Long id, String userName, String password, String userAvatar) {
        AiUser user = AiUser.builder()
                .id(id)
                .userName(userName)
                .password(password)
                .userAvatar(userAvatar)
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
                .userName(user.getUserName())
                .password(user.getPassword())
                .userRole(user.getUserRole())
                .userAvatar(user.getUserAvatar())
                .userStatus(user.getUserStatus())
                .build();
    }

}
