package com.tima.top.friends.storage.redis.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tima.top.friends.storage.redis.RedisConnector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import redis.clients.jedis.Jedis;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Repository
@Component
public class DataRedisRepository {
    public static final String PHONE_UID_KEY = "phoneuid";
    public static final String BASIC_PROFILE_KEY = "basic_profile";
    public static final String BASIC_EDUCATION_PROFILE_KEY = "basic_profile__education";
    public static final String BASIC_FAMILY_PROFILE_KEY = "basic_profile__family";
    public static final String BASIC_WORK_PROFILE_KEY = "basic_profile__work";
    public static final String GROUP_JOINED_KEY = "group_joined";
    public static final String FRIEND_LIST_KEY = "friend_list";
    public static final String FRIEND_REACTION_KEY = "friend_reaction_basic";
    public static final String FRIEND_COMMENT_KEY = "friend_comment_basic";
    public static final String FRIEND_GROUPS_KEY = "friend_group_basic";
    public static final String FRIEND_LOCATION_KEY = "friend_location_basic";
    public static final String FRIEND_CHECK_STATUS_KEY = "friend_check_status";

    @Autowired
    private RedisConnector redisConnector;

    private Jedis jedis;

    private ObjectMapper mapper;

    @PostConstruct
    private void initMapper(){
        if(mapper != null) return;
        mapper = new ObjectMapper();
    }

    public String getFidByPhone(String phone) {
        jedis = redisConnector.getJedis();
        byte[] uid = jedis.hget(PHONE_UID_KEY.getBytes(), phone.getBytes());
        return new String(uid);
    }

    public Map<String, Object> getBasisFbProfile(String fid) throws IOException {
        jedis = redisConnector.getJedis();
        String profile = jedis.hget(BASIC_PROFILE_KEY, fid);
        if(profile == null) return null;
        Map<String, Object> map = mapper.readValue(profile, Map.class);
        return map;
    }

    public List<Map<String, Object>> getBasisFbEducationProfile(String fid) throws IOException {
        jedis = redisConnector.getJedis();
        String profile = jedis.hget(BASIC_EDUCATION_PROFILE_KEY, fid);
        if(profile == null) return null;
        List<Map<String, Object>> map = mapper.readValue(profile, ArrayList.class);
        return map;
    }

    public List<Map<String, Object>> getBasisFbFamilyProfile(String fid) throws IOException {
        jedis = redisConnector.getJedis();
        String profile = jedis.hget(BASIC_FAMILY_PROFILE_KEY, fid);
        if(profile == null) return null;
        List<Map<String, Object>> map = mapper.readValue(profile, ArrayList.class);
        return map;
    }

    public List<Map<String, Object>> getBasisFbWorkProfile(String fid) throws IOException {
        jedis = redisConnector.getJedis();
        String profile = jedis.hget(BASIC_WORK_PROFILE_KEY, fid);
        if(profile == null) return null;
        List<Map<String, Object>> map = mapper.readValue(profile, ArrayList.class);
        return map;
    }

    public List<Map<String, Object>> getUserGroups(String fid) throws IOException {
        jedis = redisConnector.getJedis();
        String profile = jedis.hget(GROUP_JOINED_KEY, fid);
        if(profile == null) return null;
        List<Map<String, Object>> map = mapper.readValue(profile, ArrayList.class);
        return map;
    }

    public List<Map<String, Object>> getFriendsList(String fid) throws IOException {
        jedis = redisConnector.getJedis();
        String profile = jedis.hget(FRIEND_LIST_KEY, fid);
        if(profile == null) return null;
        List<Map<String, Object>> map = mapper.readValue(profile, ArrayList.class);
        return map;
    }

    public List<Map<String, Object>> getFriendsReaction(String fid) throws IOException {
        jedis = redisConnector.getJedis();
        String profile = jedis.hget(FRIEND_REACTION_KEY, fid);
        if(profile == null) return null;
        List<Map<String, Object>> map = mapper.readValue(profile, ArrayList.class);
        return map;
    }

    public List<Map<String, Object>> getFriendsComments(String fid) throws IOException {
        jedis = redisConnector.getJedis();
        String profile = jedis.hget(FRIEND_COMMENT_KEY, fid);
        if(profile == null) return null;
        List<Map<String, Object>> map = mapper.readValue(profile, ArrayList.class);
        return map;
    }

    public List<Map<String, Object>> getFriendsGroup(String fid) throws IOException {
        jedis = redisConnector.getJedis();
        String profile = jedis.hget(FRIEND_GROUPS_KEY, fid);
        if(profile == null) return null;
        List<Map<String, Object>> map = mapper.readValue(profile, ArrayList.class);
        return map;
    }

    public List<Map<String, Object>> getFriendsLocation(String fid) throws IOException {
        jedis = redisConnector.getJedis();
        String profile = jedis.hget(FRIEND_LOCATION_KEY, fid);
        if(profile == null) return null;
        List<Map<String, Object>> map = mapper.readValue(profile, ArrayList.class);
        return map;
    }

    public boolean checkWriteDataStatus(String fid) throws IOException {
        jedis = redisConnector.getJedis();
        String status = jedis.hget(FRIEND_CHECK_STATUS_KEY, fid);
        if("done".equals(status)) return true;
        return false;
    }

    public boolean checkKeyExist(String key, String fid) throws IOException {
        jedis = redisConnector.getJedis();
        boolean profile = jedis.hexists(key, fid);
        return profile;
    }
}
