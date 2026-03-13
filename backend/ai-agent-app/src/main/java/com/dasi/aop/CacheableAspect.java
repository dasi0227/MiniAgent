package com.dasi.aop;

import com.dasi.domain.util.jwt.UserContext;
import com.dasi.domain.util.redis.IRedisUtil;
import com.dasi.types.annotation.Cacheable;
import com.dasi.types.enumeration.CacheType;
import com.dasi.types.exception.MissingException;
import jakarta.annotation.Resource;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.dasi.types.constant.ExceptionMessage.CACHE_KEY_OR_PREFIX_REQUIRED;

@Aspect
@Component
public class CacheableAspect {

    @Resource
    private IRedisUtil redisUtil;

    @Resource
    private UserContext userContext;

    @Around("@annotation(cacheable)")
    public Object around(ProceedingJoinPoint joinPoint, Cacheable cacheable) throws Throwable {

        String cacheKey = buildCacheKey(joinPoint.getArgs(), joinPoint.getSignature().getName(), cacheable.cacheKey(), cacheable.cachePrefix());

        CacheType cacheType = cacheable.cacheType();
        Class<?> cacheClass = cacheable.cacheClass();
        long cacheTtl = cacheable.cacheTtl();

        if (!cacheKey.isBlank() && cacheClass != Object.class) {
            Object cached = readCache(cacheKey, cacheType, cacheClass);
            if (cached != null) {
                return cached;
            }
        }

        Object cacheValue = joinPoint.proceed();
        if (cacheKey.isBlank()) {
            return cacheValue;
        }

        writeCache(cacheKey, cacheType, cacheValue, cacheTtl);
        return cacheValue;
    }

    private Object readCache(String cacheKey, CacheType type, Class<?> clazz) {
        return switch (type) {
            case VALUE -> redisUtil.getValue(cacheKey, clazz);
            case LIST -> redisUtil.getList(cacheKey, clazz);
            case SET -> redisUtil.getSet(cacheKey, clazz);
            case MAP -> redisUtil.getMap(cacheKey, clazz);
        };
    }

    private void writeCache(String cacheKey, CacheType cacheType, Object cacheValue, long cacheTtl) {
        if (cacheValue == null) {
            return;
        }

        switch (cacheType) {
            case VALUE -> {
                if (cacheTtl > 0) {
                    redisUtil.setValue(cacheKey, cacheValue, cacheTtl);
                } else {
                    redisUtil.setValue(cacheKey, cacheValue);
                }
            }
            case LIST -> {
                if (cacheValue instanceof List<?> list) {
                    if (cacheTtl > 0) {
                        redisUtil.setList(cacheKey, list, cacheTtl);
                    } else {
                        redisUtil.setList(cacheKey, list);
                    }
                }
            }
            case SET -> {
                if (cacheValue instanceof Set<?> set) {
                    if (cacheTtl > 0) {
                        redisUtil.addSet(cacheKey, set, cacheTtl);
                    } else {
                        redisUtil.addSet(cacheKey, set);
                    }
                }
            }
            case MAP -> {
                if (cacheValue instanceof Map<?, ?> map) {
                    Map<String, ?> value = (Map<String, ?>) map;
                    if (cacheTtl > 0) {
                        redisUtil.setMap(cacheKey, value, cacheTtl);
                    } else {
                        redisUtil.setMap(cacheKey, value);
                    }
                }
            }
        }
    }

    private String buildCacheKey(Object[] args, String methodName, String cacheKey, String cachePrefix) {
        boolean hasCacheKey = StringUtils.hasText(cacheKey);
        boolean hasCachePrefix = StringUtils.hasText(cachePrefix);
        if (hasCacheKey == hasCachePrefix) {
            throw new MissingException(CACHE_KEY_OR_PREFIX_REQUIRED);
        }

        String baseKey;
        if (hasCacheKey) {
            baseKey = cacheKey;
        } else if (args == null || args.length == 0) {
            baseKey = cachePrefix + methodName;
        } else {
            baseKey = cachePrefix + methodName + ":" + Arrays.stream(args)
                    .map(this::safeToString)
                    .collect(Collectors.joining(","));
        }

        return appendUserScope(baseKey);
    }

    private String appendUserScope(String baseKey) {
        String role = userContext.getUserRole();
        if ("admin".equalsIgnoreCase(role)) {
            return baseKey;
        } else {
            Long userId = userContext.getUserId();
            return baseKey + ":" + userId;
        }
    }

    private String safeToString(Object arg) {
        if (arg == null) return "null";
        if (arg.getClass().isArray()) {
            if (arg instanceof Object[] objects) {
                return Arrays.deepToString(objects);
            }
            if (arg instanceof int[] ints) {
                return Arrays.toString(ints);
            }
            if (arg instanceof long[] longs) {
                return Arrays.toString(longs);
            }
            if (arg instanceof double[] doubles) {
                return Arrays.toString(doubles);
            }
            if (arg instanceof float[] floats) {
                return Arrays.toString(floats);
            }
            if (arg instanceof boolean[] booleans) {
                return Arrays.toString(booleans);
            }
            if (arg instanceof byte[] bytes) {
                return Arrays.toString(bytes);
            }
            if (arg instanceof char[] chars) {
                return Arrays.toString(chars);
            }
            if (arg instanceof short[] shorts) {
                return Arrays.toString(shorts);
            }
        }
        return String.valueOf(arg);
    }


}
