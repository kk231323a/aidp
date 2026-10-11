package com.aidp;

import com.aidp.entity.Shop;
import com.aidp.service.impl.ShopServiceImpl;
import com.aidp.utils.CacheClient;
import com.aidp.utils.RedisIdWorker;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static com.aidp.utils.RedisConstants.CACHE_SHOP_KEY;

@SpringBootTest
class AiDianPingApplicationTests {

    @Resource
    private ShopServiceImpl shopService;

    @Resource
    private RedisIdWorker redisIdWorker;

    @Resource
    private CacheClient cacheClient;

    @Test
    void  testSaveShop() throws InterruptedException {
        Shop shop=shopService.getById(1L);
        cacheClient.setWithLogicalExpire(CACHE_SHOP_KEY+ 1L, shop, 10L, TimeUnit.SECONDS);
    }
private ExecutorService es = Executors.newFixedThreadPool(500);
    @Test
    void testIdWorker() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(300);
        //多线程测试
        Runnable task = () -> {
            for (int i = 0; i < 100; i++) {
                long id = redisIdWorker.nextId("order");
                System.out.println("id = " + id);
            }
            latch.countDown();
        };
        long begin = System.currentTimeMillis();
        for (int i = 0; i < 300; i++) {
            es.submit(task);
        }
        latch.await();
        long end = System.currentTimeMillis();
        System.out.println("time = " + (end - begin));
    }


}
