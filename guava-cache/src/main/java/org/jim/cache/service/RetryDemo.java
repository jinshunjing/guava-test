package org.jim.cache.service;

import lombok.extern.slf4j.Slf4j;
import org.jim.cache.annotation.Retrying;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Slf4j
@Service
public class RetryDemo {
    private int count = 0;

    @Retrying
    public void consume(String arg) {
        log.info("consume entrance");

        if (StringUtils.isEmpty(arg)) {
            throw new IllegalArgumentException("Empty arg");
        }
        if (count < 5) {
            count++;
            log.error("consume error: {}", count);
            throw new Error("Exception " + count);
        }

        log.info("consumed: {}", arg);

        log.info("consume exit");
    }

}
