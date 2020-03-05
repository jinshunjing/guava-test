package org.jim.cache.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 封装Guava重试的注解
 *
 * @author JSJ
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Retrying {
    int attemptNumber() default 3;
    int intervalSeconds() default 5;
    Class[] retryThrowable() default {};
}
