package com.dasi.infrastructure.util;

import com.dasi.domain.util.jwt.UserContext;
import com.dasi.domain.util.random.IRandomUtil;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class RandomUtil implements IRandomUtil {

    @Resource
    private UserContext userContext;

    @Override
    public String userRandom() {
        return userContext.getUserId() + String.valueOf(System.currentTimeMillis()).substring(0, 4) + RandomStringUtils.randomAlphanumeric(4);
    }

    @Override
    public String uuid() {
        return UUID.randomUUID().toString().replace("-", "");
    }

}
