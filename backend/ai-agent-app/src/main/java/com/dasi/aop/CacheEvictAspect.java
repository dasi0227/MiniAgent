package com.dasi.aop;

import com.dasi.domain.util.redis.IRedisUtil;
import com.dasi.types.annotation.CacheEvict;
import com.dasi.types.enumeration.CacheEvictType;
import jakarta.annotation.Resource;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Aspect
@Component
public class CacheEvictAspect {

    private static final String[] USER_EVICT_PREFIX_LIST = {"ai:", "query:", "user:", "workspace:", "armory:"};
    private static final String[] ADMIN_EVICT_PREFIX_LIST = {"ai:", "query:", "admin:", "armory:"};

    @Resource
    private IRedisUtil redisUtil;

    @AfterReturning("@annotation(cacheEvict)")
    public void afterReturn(CacheEvict cacheEvict) {

        String keyPrefix = cacheEvict.keyPrefix();
        if (StringUtils.hasText(keyPrefix)) {
            redisUtil.deleteByPrefix(keyPrefix);
        }

        CacheEvictType evictType = cacheEvict.evictType();
        if (evictType != null) {
            if (evictType.equals(CacheEvictType.USER)) {
                deleteByPrefixList(USER_EVICT_PREFIX_LIST);
            } else if (evictType.equals(CacheEvictType.ADMIN)) {
                deleteByPrefixList(ADMIN_EVICT_PREFIX_LIST);
            }
        }
    }

    private void deleteByPrefixList(String[] adminEvictPrefixList) {
        for (String prefix : adminEvictPrefixList) {
            redisUtil.deleteByPrefix(prefix);
        }
    }


}
