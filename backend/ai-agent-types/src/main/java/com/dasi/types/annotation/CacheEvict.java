package com.dasi.types.annotation;

import com.dasi.types.enumeration.CacheEvictType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CacheEvict {

    String keyPrefix() default "";

    CacheEvictType evictType() default CacheEvictType.CUSTOM;

}
