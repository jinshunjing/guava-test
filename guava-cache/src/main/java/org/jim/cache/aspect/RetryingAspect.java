package org.jim.cache.aspect;

import com.github.rholder.retry.RetryerBuilder;
import com.github.rholder.retry.StopStrategies;
import com.github.rholder.retry.WaitStrategies;
import com.google.common.base.Throwables;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.jim.cache.annotation.Retrying;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * 重试注解的实现
 *
 * @author JSJ
 */
@Slf4j
@Aspect
@Component
public class RetryingAspect {
    private static final int ATTEMPT_MAX = 3;
    private static final int INTERVAL_MIN = 1;
    private static final int INTERVAL_MAX = 30;

    @Around(value = "@annotation(retryingAnnotation)")
    public Object around(ProceedingJoinPoint pjp, Retrying retryingAnnotation) throws Throwable {
        // 不重试的特殊情况
        if (retryingAnnotation.attemptNumber() <= 1) {
            return pjp.proceed();
        }

        // 处理参数
        int attemptNumber = retryingAnnotation.attemptNumber();
        if (attemptNumber > ATTEMPT_MAX) {
            attemptNumber = ATTEMPT_MAX;
        }
        int intervalSeconds = retryingAnnotation.intervalSeconds();
        if (intervalSeconds < INTERVAL_MIN) {
            intervalSeconds = INTERVAL_MIN;
        }
        if (intervalSeconds > INTERVAL_MAX) {
            intervalSeconds = INTERVAL_MAX;
        }
        List<Class> throwableList = new ArrayList<>(retryingAnnotation.retryThrowable().length);
        for (Class throwable : retryingAnnotation.retryThrowable()) {
            if (Objects.nonNull(throwable) && Throwable.class.isAssignableFrom(throwable)) {
                throwableList.add(throwable);
            }
        }

        // 构造重试管理器
        RetryerBuilder retryer = RetryerBuilder.newBuilder()
                .withStopStrategy(StopStrategies.stopAfterAttempt(attemptNumber))
                .withWaitStrategy(WaitStrategies.fixedWait(intervalSeconds, TimeUnit.SECONDS));
        if (CollectionUtils.isEmpty(throwableList)) {
            retryer.retryIfException();
        } else {
            for (Class throwable : throwableList) {
                retryer.retryIfExceptionOfType(throwable);
            }
        }

        return retryer.build().call(() -> {
            try {
                return pjp.proceed();
            } catch (Throwable t) {
                // Exception直接抛出，触发重试
                Throwables.throwIfInstanceOf(t, Exception.class);
                // Error属于严重的错误，直接退出，不会触发异常
                Throwables.throwIfUnchecked(t);
            }
            return null;
        });
    }

}
