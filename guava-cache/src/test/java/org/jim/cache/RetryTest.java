package org.jim.cache;

import org.jim.cache.service.RetryDemo;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest
@RunWith(SpringRunner.class)
public class RetryTest {
    @Autowired
    private RetryDemo demo;

    @Test
    public void testConsume() {
        System.out.println("Before consume");
        try
        {
            demo.consume("Jim");
        } catch (Throwable e) {
            System.out.println("Error consume");
            e.printStackTrace();
            // 重试N次之后还是失败，抛出异常
            // java.lang.reflect.UndeclaredThrowableException
        }
        System.out.println("After consume");
    }

}
