package com.tima.top.friends.runner;

import com.tima.top.friends.algs.RankingFriendsAlgRunner;
import com.tima.top.friends.storage.redis.repository.DataRedisRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@Component
public class AppStartupRunner implements ApplicationRunner {


    @Autowired
    private DataRedisRepository dataRedisRepository;
    /*
    *ApplicationRunner interface with a callback run() method
    *which can be invoked at application startup after the Spring application context is instantiated
    */
    @Override
    public void run(ApplicationArguments args) throws Exception {
        //excute every ... seconds
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        RankingFriendsAlgRunner worker = new RankingFriendsAlgRunner(dataRedisRepository);
        ScheduledFuture<?> result = executor.scheduleAtFixedRate(worker, 0, 600, TimeUnit.SECONDS);

        /*try {
            TimeUnit.SECONDS.sleep(30);
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
        executor.shutdown();*/
    }
}

