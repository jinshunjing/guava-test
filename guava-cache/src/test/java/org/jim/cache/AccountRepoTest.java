package org.jim.cache;

import org.jim.cache.dal.AccountRepository;
import org.jim.cache.model.Account;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest
@RunWith(SpringRunner.class)
public class AccountRepoTest {

    @Autowired
    private AccountRepository accountRepo;

    private String address = "abc";

    @Test
    public void testCache() {
        Account account = accountRepo.queryByAddress(address);
        System.out.println(account);
    }

}
