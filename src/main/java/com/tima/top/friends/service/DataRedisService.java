package com.tima.top.friends.service;

import com.tima.top.friends.storage.redis.repository.DataRedisRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.*;

@Service
public class DataRedisService {

    @Autowired
    DataRedisRepository dataRedisRepository;

    public int getNumOfCommonGroupBtwTwoUser(String fid1, String fid2) throws Exception {
        List<Map<String, Object>> list1 = dataRedisRepository.getUserGroups(fid1);
        List<Map<String, Object>> list2 = dataRedisRepository.getUserGroups(fid2);
        if(list1 == null || list2 == null) return 0;
        List<String> stringList1 = list1.stream().map(map -> String.valueOf(map.get("gid"))).collect(Collectors.toList());
        List<String> stringList2 = list2.stream().map(map -> String.valueOf(map.get("gid"))).collect(Collectors.toList());
        return getSizeOfIntersectionSet(stringList1, stringList2);
    }

    public int getNumOfCommonGroupBtwTwoUser(List<Map<String, Object>> listGrOfUser, String fidOfFriend, Map<String, List<String>> friendsGrMap) throws Exception {
        List<String> gidList2 = friendsGrMap.get(fidOfFriend);
        if(listGrOfUser == null || gidList2 == null) return 0;
        List<String> gidList1 = listGrOfUser.stream().map(map -> String.valueOf(map.get("gid"))).collect(Collectors.toList());
        return getSizeOfIntersectionSet(gidList1, gidList2);
    }


    public int getNumOfCommonFriendsBtwTwoUser(String fid1, String fid2) throws Exception {
        List<Map<String, Object>> list1 = dataRedisRepository.getFriendsList(fid1);
        List<Map<String, Object>> list2 = dataRedisRepository.getFriendsList(fid2);
        if(list1 == null || list2 == null) return 0;
        List<String> stringList1 = list1.stream().map(map -> String.valueOf(map.get("fid"))).collect(Collectors.toList());
        List<String> stringList2 = list2.stream().map(map -> String.valueOf(map.get("fid"))).collect(Collectors.toList());
        return getSizeOfIntersectionSet(stringList1, stringList2);
    }

    private int getSizeOfIntersectionSet(List<String> list, List<String> otherList){
        Set<String> result = list.stream()
                .distinct()
                .filter(otherList::contains)
                .collect(Collectors.toSet());

        return result!=null ? result.size() : 0;
    }

    public int getDistanceFromLatLongOfTwoUser(String fid1, String fid2) throws Exception {
        Map<String, Object> map = dataRedisRepository.getBasisFbProfile(fid1);
        Map<String, Object> map1 = dataRedisRepository.getBasisFbProfile(fid2);

        if(map == null || map1 == null) return -1;

        if(map.get("currentLocationLatitude") == null
                || map.get("currentLocationLongitude") == null
                || map1.get("currentLocationLatitude") == null
                || map1.get("currentLocationLatitude") == null)
        {
            return -1;
        }
        return calculateDistanceInKilometer((Double)map.get("currentLocationLatitude"),
                (Double)map.get("currentLocationLongitude"),
                (Double)map1.get("currentLocationLatitude"),
                (Double)map1.get("currentLocationLongitude"));
    };

    public int getDistanceFromLatLongOfTwoUser(String fid1, String fid2, Map<String, Map<String, Object>> friendsLocation) throws Exception {
        Map<String, Object> map = dataRedisRepository.getBasisFbProfile(fid1);
        Map<String, Object> map1 =friendsLocation.get(fid2);

        if(map == null || map1 == null) return -1;

        if(map.get("currentLocationLatitude") == null
                || map.get("currentLocationLongitude") == null
                || map1.get("currentLocationLatitude") == null
                || map1.get("currentLocationLongitude") == null)
        {
            return -1;
        }
        return calculateDistanceInKilometer((Double)map.get("currentLocationLatitude"),
                (Double)map.get("currentLocationLongitude"),
                (Double)map1.get("currentLocationLatitude"),
                (Double)map1.get("currentLocationLongitude"));
    };

    public final static double AVERAGE_RADIUS_OF_EARTH_KM = 6371;
    private int calculateDistanceInKilometer(double userLat, double userLng,
                                            double venueLat, double venueLng) {

        double latDistance = Math.toRadians(userLat - venueLat);
        double lngDistance = Math.toRadians(userLng - venueLng);

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(userLat)) * Math.cos(Math.toRadians(venueLat))
                * Math.sin(lngDistance / 2) * Math.sin(lngDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return (int) (Math.round(AVERAGE_RADIUS_OF_EARTH_KM * c));
    }

    public Map<String, List<String>> getFriendsGroup(String fid) throws Exception {
        List<Map<String, Object>> listFriendGroup = dataRedisRepository.getFriendsGroup(fid);
        //group by theo fid ->  tìm các group mà các friends tham gia
        Map<String, List<String>> mapGr = listFriendGroup.stream()
                .collect(Collectors.groupingBy(m -> String.valueOf(m.get("fid")),
                        Collectors.mapping(m -> String.valueOf(m.get("gid")), Collectors.toList())));

        return mapGr;
    }

    public Map<String, Map<String, Object>>  getFriendsLocation(String fid) throws Exception {
        List<Map<String, Object>> listFriendGroup = dataRedisRepository.getFriendsLocation(fid);
        //group by theo fid ->  tìm friends location
        Map<String, Map<String, Object>> mapLocation = listFriendGroup.stream()
                .collect(Collectors.toMap(m -> String.valueOf(m.get("fid")), m -> {
                    Map<String, Object> location = new HashMap<>();
                    location.put("currentLocationLatitude",  m.get("currentLocationLatitude"));
                    location.put("currentLocationLongitude",  m.get("currentLocationLongitude"));
                    return location;
                }, (value1, value2) -> value2));

        return mapLocation;
    }

    public int getNumCommentsOfFriend(List<Map<String, Object>> listCmts, String friendFid) throws Exception {
        int n = 0;
        for(Map<String, Object> map : listCmts){
            if(friendFid.equals(String.valueOf(map.get("fromId")))){
                n = Integer.parseInt(String.valueOf(map.get("total")));
                break;
            }
        }
        return n;
    }

    public int getNumLikeOfFriend(List<Map<String, Object>> listReaction, String friendFid) throws Exception {
        int n = 0;
        for(Map<String, Object> map : listReaction){
            if(friendFid.equals(String.valueOf(map.get("fid")))
                    && "LIKE".equals(String.valueOf(map.get("type")))){
                n = Integer.parseInt(String.valueOf(map.get("total")));
                break;
            }
        }
        return n;
    }

    public int getNumHahaOfFriend(List<Map<String, Object>> listReaction, String friendFid) throws Exception {
        int n = 0;
        for(Map<String, Object> map : listReaction){
            if(friendFid.equals(String.valueOf(map.get("fid")))
                    && "HAHA".equals(String.valueOf(map.get("type")))){
                n = Integer.parseInt(String.valueOf(map.get("total")));
                break;
            }
        }
        return n;
    }

    public int getNumLoveOfFriend(List<Map<String, Object>> listReaction, String friendFid) throws Exception {
        int n = 0;
        for(Map<String, Object> map : listReaction){
            if(friendFid.equals(String.valueOf(map.get("fid")))
                    && "LOVE".equals(String.valueOf(map.get("type")))){
                n = Integer.parseInt(String.valueOf(map.get("total")));
                break;
            }
        }
        return n;
    }

    public int getNumSadOfFriend(List<Map<String, Object>> listReaction, String friendFid) throws Exception {
        int n = 0;
        for(Map<String, Object> map : listReaction){
            if(friendFid.equals(String.valueOf(map.get("fid")))
                    && "SAD".equals(String.valueOf(map.get("type")))){
                n = Integer.parseInt(String.valueOf(map.get("total")));
                break;
            }
        }
        return n;
    }

    public int getNumWowOfFriend(List<Map<String, Object>> listReaction, String friendFid) throws Exception {
        int n = 0;
        for(Map<String, Object> map : listReaction){
            if(friendFid.equals(String.valueOf(map.get("fid")))
                    && "WOW".equals(String.valueOf(map.get("type")))){
                n = Integer.parseInt(String.valueOf(map.get("total")));
                break;
            }
        }
        return n;
    }

    public int getNumAngryOfFriend(List<Map<String, Object>> listReaction, String friendFid) throws Exception {
        int n = 0;
        for(Map<String, Object> map : listReaction){
            if(friendFid.equals(String.valueOf(map.get("fid")))
                    && "ANGRY".equals(String.valueOf(map.get("type")))){
                n = Integer.parseInt(String.valueOf(map.get("total")));
                break;
            }
        }
        return n;
    }

    public int getTotalReactionOfFriend(List<Map<String, Object>> listReaction, String friendFid) throws Exception {
       int totalReaction = listReaction.stream().filter(map -> friendFid.equals(String.valueOf(map.get("fid"))))
               .map(map -> Integer.parseInt(String.valueOf(map.get("total"))))
               .reduce(0, (x,y) -> x + y);
        return listReaction == null || listReaction.isEmpty() ? 0 : totalReaction;
    }

    public int getTotalCommentOfFriend(Map<String, Integer> commentMap, String friendFid) throws Exception {
        if(!commentMap.containsKey(friendFid)) return 0;
        return commentMap.get(friendFid);
    }
}

