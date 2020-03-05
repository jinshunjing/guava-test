package org.jim.cache;

import com.google.common.util.concurrent.RateLimiter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.concurrent.TimeUnit;

/**
 * 令牌桶限速
 */
@RunWith(SpringRunner.class)
public class RateLimiterTest {

    @Test
    public void testRateLimiter() throws Exception {
        demoRateLimiter();
    }

    private void demoRateLimiter() throws Exception{
        // 每秒2个令牌
        RateLimiter rateLimiter = RateLimiter.create(2);

        for (int i = 0; i < 10; i++) {
            long st = System.currentTimeMillis();
            boolean acquired = rateLimiter.tryAcquire(1L, TimeUnit.SECONDS);
            long ct = System.currentTimeMillis() - st;
            System.out.println(i + ": " + acquired + ", " + ct + "ms");

            if (i == 3) {
                Thread.sleep(2000L);
            }
        }
    }

}
