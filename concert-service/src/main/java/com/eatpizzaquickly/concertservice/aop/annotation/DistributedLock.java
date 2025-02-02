package com.eatpizzaquickly.concertservice.aop.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DistributedLock {

    String key();

    TimeUnit timeUnit() default TimeUnit.SECONDS;

    // 락 획들을 위해 waitTime 만큼 대기한다.
    long waitTime() default 5L;

    // 락을 획득한 이후 leaseTime 이 지나면 락을 해제한다.
    long leaseTime() default 3L;
}
