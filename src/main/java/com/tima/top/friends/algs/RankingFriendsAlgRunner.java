package com.tima.top.friends.algs;

import com.tima.top.friends.storage.redis.repository.DataRedisRepository;

public class RankingFriendsAlgRunner implements Runnable {

    private DataRedisRepository dataRedisRepository;

    public RankingFriendsAlgRunner(DataRedisRepository dataRedisRepository) {
        this.dataRedisRepository = dataRedisRepository;
    }

    @Override
    public void run() {
        /*try {
            Map<String, Object> map = dataRedisRepository.getBasisFbProfile("1794646613");
            Map<String, Object> map1 = dataRedisRepository.getBasisFbProfile("100007595923547");

            List<Map<String, Object>> list1 = dataRedisRepository.getBasisFbEducationProfile("100006678397581");
            List<Map<String, Object>> list2 = dataRedisRepository.getBasisFbFamilyProfile("100006678397581");
            List<Map<String, Object>> list3 = dataRedisRepository.getBasisFbWorkProfile("100005602024663");
            System.out.println(map);
        } catch (IOException e) {
            e.printStackTrace();
        }*/
    }

}
