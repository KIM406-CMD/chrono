package com.ncist.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 公开接口注解：标记无需登录即可访问的接口
 * 由 JwtInterceptor 扫描方法/类上的该注解放行，替代路径白名单，避免通配符误放行
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Public {
}
