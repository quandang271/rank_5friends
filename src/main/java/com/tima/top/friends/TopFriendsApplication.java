package com.tima.top.friends;

import com.tima.top.friends.service.TopFriendsService;
import com.tima.top.friends.storage.mongo.model.Vay1hCustomer;
import com.tima.top.friends.storage.mongo.repository.CustomerRepository;
import com.tima.top.friends.storage.redis.RedisConnector;
import com.tima.top.friends.storage.redis.repository.DataRedisRepository;
import com.tima.top.friends.service.DataRedisService;
import com.tima.top.friends.service.DataMongoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@SpringBootApplication
@EntityScan(basePackages = "com.tima.top.friends.*")
@EnableMongoRepositories(basePackages = "com.tima.top.friends.*")
public class TopFriendsApplication {

    @Autowired
    private DataRedisRepository phoneUidRepository;

    @Autowired
    private DataRedisService dataRedisService;

    @Autowired
    private TopFriendsService topFriendsService;

    @Autowired
    private RedisConnector redisConnector;

    @Autowired
    DataRedisRepository dataRedisRepository;


    public static void main(String[] args) {
        SpringApplication.run(TopFriendsApplication.class, args);
    }

    ;


    @Bean
    CommandLineRunner init(CustomerRepository customerRepository, DataMongoService getInfoMongoService) {

        return args -> {

/*           Vay1hCustomer obj = customerRepository.findById("5d2323983596034754c7d7f0").orElse(null);
            System.out.println(obj);

            Map<String, String> phones = getInfoMongoService.genPhoneNumberListOfCustomer(obj.getListcontact());
            Map<String, String> fphones = getInfoMongoService.getFamilyMemberPhone("5d2323983596034754c7d7f0");
            System.out.println(phones);


            int n = dataRedisService.getNumOfCommonGroupBtwTwoUser("100009310778763", "100000720273835");
            System.out.println(n);

            int d = dataRedisService.getDistanceFromLatLongOfTwoUser("1794646613", "100007595923547");
            System.out.println(d);


            List<Map<String, Object>> listFriend = dataRedisRepository.getFriendsList("100003642871882");
            System.out.println(listFriend);

            Map<String, List<String>> mapGr = dataRedisService.getFriendsGroup("100003642871882");
            System.out.println("'");

            List<Map<String, Object>> listFr1 = dataRedisRepository.getUserGroups("100003642871882");

            List<Map<String, Object>> listreaction = dataRedisRepository.getFriendsReaction("100003642871882");

            List<Map<String, Object>> listFriendFilter =  listFriend.stream().filter(map -> {
                try {
                    return dataRedisService.getTotalReactionOfFriend(listreaction, String.valueOf(map.get("fid"))) >=4;
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return false;
            }).collect(Collectors.toList());
            for(Map<String, Object> map : listFriendFilter){
                int n = 0;
                try {
                    int totalRaction = dataRedisService.getTotalReactionOfFriend(listreaction, String.valueOf(map.get("fid")));
                    if(totalRaction<5) continue;
                    n = dataRedisService.getNumOfCommonGroupBtwTwoUser(listFr1, String.valueOf(map.get("fid")), mapGr);
                    System.out.println(n);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }

            float[][] input = topFriendsService.initInputForArfcAlg("100003642871882");
            System.out.println(Arrays.deepToString(input));

            List<Map<String, Object>> locations = dataRedisRepository.getFriendsLocation("100003642871882");
            System.out.println(locations);
            List<Map<String, String>> fbF = topFriendsService.getFbFamily("100003642871882");
            System.out.println(fbF);
            System.out.println(dataRedisRepository.getUserGroups("100004374336661")); */
            System.out.println(dataRedisRepository.getFriendsLocation("100010093962788"));
            List<Map<String,Object>> listFriends = dataRedisRepository.getFriendsList("100004374336661");
            for (Map map:listFriends){
                String id = "";
                if(map.get("fid") != null)
                    id = map.get("fid").toString();
                System.out.println(id);
                System.out.println(dataRedisRepository.getUserGroups(id));
            }
          //  System.out.println(dataRedisRepository.getBasisFbEducationProfile("100032922890513"));
        };
    }
}