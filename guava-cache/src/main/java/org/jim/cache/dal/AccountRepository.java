package org.jim.cache.dal;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import org.jim.cache.model.Account;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author JSJ
 */
@Slf4j
@Component
public class AccountRepository {

    /** 本地缓存 */
    private LoadingCache<String, Account> localCache;

    /**
     * 表示不存在的个人账户
     */
    private static Account EMPTY_ACCOUNT = Account.builder().build();

    @PostConstruct
    public void init() {
        // 初始化本地缓存和索引
        localCache = CacheBuilder.newBuilder()
                .maximumSize(128L)
                .expireAfterWrite(30, TimeUnit.MINUTES)
                .build(new AccountRepository.AccountCacheLoader());
    }

    /**
     * 本地缓存不命中时自动加载记录
     */
    private final class AccountCacheLoader extends CacheLoader<String, Account> {
        @Override
        public Account load(String key) {
            Account record = queryByAddressInternal(key);
            // FIXME: 填充查询不到的记录
            if (Objects.isNull(record)) {
                record = EMPTY_ACCOUNT;
            }
            return record;
        }
        @Override
        public Map<String, Account> loadAll(Iterable<? extends  String> keys) {
            List<String> codeList = new ArrayList<>();
            for (String key : keys) {
                codeList.add(key);
            }
            List<Account> records = listByAddressInternal(codeList);
            if (null == records) {
                return Collections.emptyMap();
            }
            Map<String, Account> aMap = records.stream().collect(
                    Collectors.toMap(Account::getAddress, Function.identity(), (a, b) -> a));
            // FIXME: 填充查询不到的记录
            for (String key : keys) {
                if (!aMap.containsKey(key)) {
                    aMap.put(key, EMPTY_ACCOUNT);
                }
            }
            return aMap;
        }
    }

    /**
     * 先查询缓存
     *
     * @param address
     * @return
     */
    public Account queryByAddress(String address) {
        try {
            Account record = localCache.get(address);
            if (record == EMPTY_ACCOUNT) {
                record = null;
            }
            return record;
        } catch (ExecutionException e) {
            log.error("DB error", e);
            return null;
        }
    }

    /**
     * 先查询缓存
     *
     * @param addresses
     * @return
     */
    public List<Account> listByAddress(List<String> addresses) {
        try {
            ImmutableMap<String, Account> map = localCache.getAll(addresses);
            if (map.isEmpty()) {
                return Collections.emptyList();
            }
            List<Account> results = new ArrayList<>();
            for (Account record : map.values()) {
                if (record != EMPTY_ACCOUNT) {
                    results.add(record);
                }
            }
            return results;
        } catch (ExecutionException e) {
            log.error("DB error", e);
            return Collections.emptyList();
        }
    }

    /**
     * 删除缓存
     *
     * @param address
     */
    public void deleteByAddress(String address) {
        localCache.invalidate(address);
    }

    private Account queryByAddressInternal(String address) {
        // TODO
        if (address.length() < 5) {
            return null;
        }
        return Account.builder().address(address).build();
    }

    private List<Account> listByAddressInternal(List<String> addrList) {
        // TODO
        if (addrList.size() < 2) {
            return Collections.emptyList();
        }
        Account a1 = Account.builder().address(addrList.get(0)).build();
        return Arrays.asList(a1);
    }

}
