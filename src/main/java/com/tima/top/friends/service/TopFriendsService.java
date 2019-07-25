package com.tima.top.friends.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tima.top.friends.algs.arfc.ArfcAlgorithm;
import com.tima.top.friends.common.body.request.TopFriendsRequest;
import com.tima.top.friends.common.body.response.ResponseBodyObject;
import com.tima.top.friends.common.body.response.TopFriendResultResponse;
import com.tima.top.friends.common.body.response.TopFriendsResponse;
import com.tima.top.friends.http.HttpApi;
import com.tima.top.friends.storage.mongo.model.Vay1hCustomer;
import com.tima.top.friends.storage.mongo.repository.CustomerRepository;
import com.tima.top.friends.storage.postgres.entities.RequestLog;
import com.tima.top.friends.storage.postgres.entities.TopFriendFb;
import com.tima.top.friends.storage.postgres.repository.RequestLogRepository;
import com.tima.top.friends.storage.postgres.repository.TopFriendsFbRepository;
import com.tima.top.friends.storage.redis.repository.DataRedisRepository;
import org.eclipse.jetty.client.api.ContentResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TopFriendsService {

    public static Logger LOG = LoggerFactory.getLogger(TopFriendsService.class);
    protected static final int ERROR_CODE = 1;

    @Autowired
    private DataRedisService dataRedisService;

    @Autowired
    private RequestLogRepository requestLogRepository;

    @Autowired
    private TopFriendsFbRepository topFriendsFbRepository;

    @Autowired
    private DataRedisRepository dataRedisRepository;

    @Autowired
    private DataMongoService dataMongoService;

    @Autowired
    private CustomerRepository customerRepository;

    protected Random rndRefCode = new Random();

    @Value("${top.friends.api.search-fb}")
    private String urlSearchFb;

    @Value("${top.friends.api.search-friend}")
    private String urlSearchFriends;

    /*private static final String[] features = new String[]{"n_common_groups", "distance_fb_lat_long"
            , "like_count", "haha_count", "wow_count", "sad_count", "angry_count", "love_count", "comment_count"};*/
    private static final String[] features = new String[]{"n_common_groups", "distance_fb_lat_long"
            , "reaction_count", "comment_count"};
    //private static int[] attitudeFeatureVector = new int[]{1, 0, 1, 1, 1, 1, 1, 1, 1};
    private static int[] attitudeFeatureVector = new int[]{1, 0, 1, 1};

    private Map<Integer, String> mapIndexFid;

    public TopFriendsResponse getTopFriendsByReferenceCode(String referenceCode) {
        TopFriendsResponse response = new TopFriendsResponse();

        LOG.info("get top friends by reference code: " + referenceCode);
        RequestLog logRow = requestLogRepository.findById(referenceCode).orElse(null);
        if (logRow == null) {
            response.setResponseCode(ERROR_CODE);
            response.setMess("Ko tìm thấy request tương ứng với reference code");
            return response;
        }
        ResponseBodyObject.Status statusOfRef = ResponseBodyObject.Status.valueOf(logRow.getStatus());

        TopFriendResultResponse result = new TopFriendResultResponse();
        result.setReferenceCode(referenceCode);
        result.setStatus(statusOfRef);
        result.setStatusDes(logRow.getStatusDes());

        response.setResponseCode(0);
        response.setMess("Thành công");
        LOG.info("Reference Code: " + referenceCode + ", status: " + statusOfRef);

        switch (statusOfRef) {
            case DONE:
                TopFriendFb item = topFriendsFbRepository.findByReferenceCode(referenceCode);
                if (item.getTopFriends() == null || item.getTopFriends() == "") {
                    result.setDetails(new ArrayList<>());
                }
                result.setDetails(Arrays.asList(item.getTopFriends().split(",")));
                result.setFamilyFb(getFbFamily(item.getFid()));
                result.setFamilyPhone(dataMongoService.getFamilyMemberPhone(item.getUid()));
                response.setResult(result);
                LOG.info("top n frineds : {}", result.getDetails());
                return response;
            default:
                response.setResult(result);
                return response;
        }
    }

    public TopFriendsResponse getTopFriends(TopFriendsRequest request, String fid) {
        TopFriendsResponse response;
        ObjectMapper mapper = new ObjectMapper();
        try {
            String jsonString = mapper.writeValueAsString(request);

            LOG.info("receiver request body {}", request);
            response = new TopFriendsResponse();
            response.setResponseCode(0);
            response.setMess("Thành công");
            TopFriendResultResponse result;

            TopFriendFb topFriend = topFriendsFbRepository.findByFacabookId(fid);
            if (topFriend != null) {
                result = new TopFriendResultResponse(ResponseBodyObject.StatusDefaultDone.STATUS,
                        ResponseBodyObject.StatusDefaultDone.STATUS_DES,
                        Arrays.asList(topFriend.getTopFriends().split(",")));
                result.setFamilyFb(getFbFamily(fid));
                result.setFamilyPhone(dataMongoService.getFamilyMemberPhone(request.getUid()));
                response.setResult(result);
                LOG.info("fid: {}, top friends: {}", fid, Arrays.asList(topFriend.getTopFriends().split(",")));
                return response;
            }

            String referenceCode = genReferenceCode();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        boolean checkExistKeyRedis = dataRedisRepository.checkKeyExist(DataRedisRepository.FRIEND_CHECK_STATUS_KEY, fid);
                        if (!checkExistKeyRedis) {
                            callApiSearchFb(fid);
                            callApiSearchFriends(fid);
                        }
                        boolean checkDataRedis = dataRedisRepository.checkWriteDataStatus(fid);
                        long start = System.currentTimeMillis();
                        boolean isTimeout = false;
                        while (!checkDataRedis) {
                            Thread.sleep(3000);
                            checkDataRedis = dataRedisRepository.checkWriteDataStatus(fid);

                            //running time
                            long elapsedTimeMillis = System.currentTimeMillis() - start;
                            float elapsedTimeMin = elapsedTimeMillis / (60 * 1000F);
                            if (elapsedTimeMin > 5f) {
                                RequestLog log = requestLogRepository.findById(referenceCode).orElse(null);
                                if (log == null) {
                                    log = new RequestLog(referenceCode, ResponseBodyObject.StatusDefaultTimeout.STATUS.toString(), ResponseBodyObject.StatusDefaultTimeout.STATUS_DES);
                                    log.setRequestBody(jsonString);
                                } else {
                                    log.setStatus(ResponseBodyObject.StatusDefaultTimeout.STATUS.toString());
                                    log.setStatusDes(ResponseBodyObject.StatusDefaultTimeout.STATUS_DES);
                                    log.setRequestBody(jsonString);
                                }
                                requestLogRepository.save(log);
                                isTimeout = true;
                                LOG.info("Timeout when watting data from redis!!!!!!!!!");
                                break;

                            }
                        }
                        if (!isTimeout) {
                            List<String> topN = getTopNFriends(fid, 5);
                            if (topN == null) {
                                RequestLog log = requestLogRepository.findById(referenceCode).orElse(null);
                                if (log != null) {
                                    log.setStatus(ResponseBodyObject.StatusDefaultBad.STATUS.toString());
                                    log.setStatusDes(ResponseBodyObject.StatusDefaultBad.STATUS_DES);
                                    log.setLastUpdate(new Timestamp(System.currentTimeMillis()));
                                    requestLogRepository.save(log);
                                    LOG.info("update request log done! reference_code: {}", referenceCode);
                                }
                            } else {
                                saveTopFriendsResult(request.getUid(), fid, referenceCode, topN);
                            }
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();

            // save status log
            RequestLog log = requestLogRepository.findById(referenceCode).orElse(null);
            if (log == null) {
                log = new RequestLog(referenceCode, ResponseBodyObject.StatusDefaultProcessing.STATUS.toString(), ResponseBodyObject.StatusDefaultProcessing.STATUS_DES);
                log.setRequestBody(jsonString);
                requestLogRepository.save(log);
            }

            result = new TopFriendResultResponse();
            result.setReferenceCode(referenceCode);
            result.setStatus(ResponseBodyObject.StatusDefaultProcessing.STATUS);
            result.setStatusDes(ResponseBodyObject.StatusDefaultProcessing.STATUS_DES);
            response.setResult(result);

        } catch (JsonProcessingException e) {
            response = new TopFriendsResponse();
            response.setResponseCode(1);
            response.setMess(e.getMessage());
            e.printStackTrace();
        }
        return response;
    }

    public String getPhoneByUid(String uid) {
        Vay1hCustomer customer = customerRepository.findById(uid).orElse(null);
        if (customer != null) {
            return customer.getPhone();
        }
        return null;
    }

    public List<Map<String, String>> getFbFamily(String fid) {
        try {
            List<Map<String, Object>> fbFamily = dataRedisRepository.getBasisFbFamilyProfile(fid);
            List<Map<String, String>> fbFamilyResponse = new ArrayList<>();
            for (Map<String, Object> map : fbFamily) {
                Map<String, String> fb = new HashMap<>();
                fb.put("fid", String.valueOf(map.get("memberId")));
                fb.put("relationship", String.valueOf(map.get("relationship")));
                fbFamilyResponse.add(fb);
            }
            return fbFamilyResponse;
        } catch (IOException e) {
            return null;
        }
    }

    private void saveTopFriendsResult(String uid, String fid, String referenceCode, List<String> topN) {
        // save status log
        RequestLog log = requestLogRepository.findById(referenceCode).orElse(null);
        if (log != null) {
            log.setStatus(ResponseBodyObject.StatusDefaultDone.STATUS.toString());
            log.setStatusDes(ResponseBodyObject.StatusDefaultDone.STATUS_DES);
            log.setLastUpdate(new Timestamp(System.currentTimeMillis()));
            requestLogRepository.save(log);
            LOG.info("update request log done! reference_code: {}", referenceCode);
        }
        String topNstr = String.join(",", topN);

        TopFriendFb topFriendFb = new TopFriendFb();
        topFriendFb.setReferenceCode(referenceCode);
        topFriendFb.setFid(fid);
        topFriendFb.setUid(uid);
        topFriendFb.setTopFriends(topNstr);
        topFriendFb.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
        topFriendsFbRepository.save(topFriendFb);
        LOG.info("save top n friend of fid {} done!!!!!!!!, top n: {}", fid, topN);
    }

    protected String genReferenceCode() {
        return String.valueOf(Math.abs(rndRefCode.nextLong()));
    }

    public List<String> getTopNFriends(String fid, int nFriends) throws Exception {
        LOG.info("start process to find top n friends........");
        float[][] inputMatrix = initInputForArfcAlg(fid);
        if (inputMatrix == null) return null;
        ArfcAlgorithm arfcAlgorithm = new ArfcAlgorithm(inputMatrix, attitudeFeatureVector);
        arfcAlgorithm.scoring();
        Float[] score = arfcAlgorithm.getScore();
        Map<String, Float> mapFidScore = new HashMap<>();
        for (int i = 0; i < score.length; i++) {
            mapFidScore.put(mapIndexFid.get(i), score[i]);
        }
        Map<String, Float> mapFidScoreRanking = mapFidScore.entrySet()
                .stream()
                .sorted((Map.Entry.<String, Float>comparingByValue().reversed()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
        List<String> topN = mapFidScoreRanking.entrySet().stream()
                .map(Map.Entry::getKey)
                .limit(nFriends)
                .collect(Collectors.toList());
        return topN;
    }

    public float[][] initInputForArfcAlg(String fid) throws Exception {
        List<Map<String, Object>> listFriends = dataRedisRepository.getFriendsList(fid);
        float[][] inputMatrix = null;
        if (listFriends != null && !listFriends.isEmpty()) {
            List<Map<String, Object>> listFriendFilter = filterListFriends(fid, listFriends);
            if (listFriendFilter == null || listFriendFilter.isEmpty()) return null;
            Map<String, Map<String, Object>> dataFriends = getDataFriendsFromRedis(fid, listFriendFilter);
            int m = listFriendFilter.size();
            int n = features.length;
            List<String> keys = new ArrayList<>(dataFriends.keySet());
            inputMatrix = new float[m][n];
            mapIndexFid = new HashMap<>();
            for (int i = 0; i < m; i++) {
                for (int j = 0; j < n; j++) {
                    Map<String, Object> fi = dataFriends.get(keys.get(i));
                    inputMatrix[i][j] = fi.get(features[j]) != null ? (int) fi.get(features[j]) : -1;
                }
                mapIndexFid.put(i, keys.get(i));
            }

        }
        normalizeInputMatrix(inputMatrix);
        return inputMatrix;
    }

    public void normalizeInputMatrix(float[][] input) {
        //thay value -1 bang khoang cach trung binh
        int m = input.length;
        int n = input[0].length;
        int count = 0;
        float sum = 0f;
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                if (input[i][1] != -1) {
                    sum += input[i][1];
                    count++;
                }
            }
        }
        if (count == 0) return;
        float avgLocationDistance = sum / count;
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                if (input[i][1] == -1) {
                    input[i][1] = avgLocationDistance;
                }
            }
        }
    }

    private Map<String, Map<String, Object>> getDataFriendsFromRedis(String fid, List<Map<String, Object>> listFriends) throws Exception {
        Map<String, Map<String, Object>> resultmap = new HashMap<>();
        for (Map<String, Object> friend : listFriends) {
            try {
                addFriendFeatures(fid, friend, resultmap);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return resultmap;
    }

    public List<Map<String, Object>> filterListFriends(String fid, List<Map<String, Object>> listFriends) throws Exception {
        // bỏ những friends có tổng số reaction < 5 || tong so comment < 3
        List<Map<String, Object>> listreaction = dataRedisRepository.getFriendsReaction(fid);
        List<Map<String, Object>> listcmt = dataRedisRepository.getFriendsComments(fid);
        Map<String, Integer> cmtsMap = listcmt.stream()
                .collect(Collectors.toMap(m -> String.valueOf(m.get("fromId")),
                        m -> Integer.parseInt(String.valueOf(m.get("total")))));
        List<Map<String, Object>> listFriendFilter = listFriends.stream().filter(map -> {
            try {
                return dataRedisService.getTotalReactionOfFriend(listreaction, String.valueOf(map.get("fid"))) >= 5
                        || dataRedisService.getTotalCommentOfFriend(cmtsMap,String.valueOf(map.get("fid"))) >= 3;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return false;
        }).collect(Collectors.toList());
        return listFriendFilter;
    }

    private void addFriendFeatures(String fid, Map<String, Object> friend, Map<String, Map<String, Object>> resultmap) throws Exception {
        String friendId = String.valueOf(friend.get("fid"));
        Map<String, Object> featureMap = resultmap.get(friendId);
        if (featureMap == null) {
            resultmap.put(friendId, new HashMap<>());
        }

        Map<String, List<String>> mapFriendGrs = dataRedisService.getFriendsGroup(fid);
        List<Map<String, Object>> listUserGrs = dataRedisRepository.getUserGroups(fid);
        Map<String, Map<String, Object>> friendsLocation = dataRedisService.getFriendsLocation(fid);

        addCommonGroupFeature(listUserGrs, mapFriendGrs, friend, resultmap.get(friendId));
        //addCommonFriendFeature(fid, friend, resultmap.get(friendId));
        addDistanceFBLocationFeature(fid, friend, friendsLocation, resultmap.get(friendId));

        List<Map<String, Object>> listreaction = dataRedisRepository.getFriendsReaction(fid);
        /*addNumOfLikeFeature(friendId, listreaction, resultmap.get(friendId));
        addNumOfHahaFeature(friendId, listreaction, resultmap.get(friendId));
        addNumOfLoveFeature(friendId, listreaction, resultmap.get(friendId));
        addNumOfWowFeature(friendId, listreaction, resultmap.get(friendId));
        addNumOfSadFeature(friendId, listreaction, resultmap.get(friendId));
        addNumOfAngryFeature(friendId, listreaction, resultmap.get(friendId));*/
        addNumOfReactionFeature(friendId, listreaction, resultmap.get(friendId));

        List<Map<String, Object>> listcmts = dataRedisRepository.getFriendsComments(fid);
        addNumOfCommentFeature(friendId, listcmts, resultmap.get(friendId));

    }

    private void addDistanceFBLocationFeature(String fid, Map<String, Object> friend, Map<String, Map<String, Object>> friendsLocation, Map<String, Object> featureMap) throws Exception {
        String friendId = String.valueOf(friend.get("fid"));
        int n = dataRedisService.getDistanceFromLatLongOfTwoUser(fid, friendId, friendsLocation);
        featureMap.put("distance_fb_lat_long", n);
    }

    private void addCommonGroupFeature(List<Map<String, Object>> listGrOfUser,
                                       Map<String, List<String>> friendsGrMap,
                                       Map<String, Object> friend,
                                       Map<String, Object> featureMap) throws Exception {
        String friendId = String.valueOf(friend.get("fid"));
        int n = dataRedisService.getNumOfCommonGroupBtwTwoUser(listGrOfUser, friendId, friendsGrMap);
        featureMap.put("n_common_groups", n);
    }

    private void addCommonFriendFeature(String fid, Map<String, Object> friend, Map<String, Object> featureMap) throws Exception {
        String friendId = String.valueOf(friend.get("fid"));
        int n = dataRedisService.getNumOfCommonFriendsBtwTwoUser(fid, friendId);
        featureMap.put("n_common_friends", n);
    }

    private void addNumOfLikeFeature(String friendFid, List<Map<String, Object>> listreaction, Map<String, Object> featureMap) throws Exception {
        int n = dataRedisService.getNumLikeOfFriend(listreaction, friendFid);
        featureMap.put("like_count", n);
    }

    private void addNumOfHahaFeature(String friendFid, List<Map<String, Object>> listreaction, Map<String, Object> featureMap) throws Exception {
        int n = dataRedisService.getNumHahaOfFriend(listreaction, friendFid);
        featureMap.put("haha_count", n);
    }

    private void addNumOfLoveFeature(String friendFid, List<Map<String, Object>> listreaction, Map<String, Object> featureMap) throws Exception {
        int n = dataRedisService.getNumLoveOfFriend(listreaction, friendFid);
        featureMap.put("love_count", n);
    }

    private void addNumOfSadFeature(String friendFid, List<Map<String, Object>> listreaction, Map<String, Object> featureMap) throws Exception {
        int n = dataRedisService.getNumSadOfFriend(listreaction, friendFid);
        featureMap.put("sad_count", n);
    }

    private void addNumOfWowFeature(String friendFid, List<Map<String, Object>> listreaction, Map<String, Object> featureMap) throws Exception {
        int n = dataRedisService.getNumWowOfFriend(listreaction, friendFid);
        featureMap.put("wow_count", n);
    }

    private void addNumOfAngryFeature(String friendFid, List<Map<String, Object>> listreaction, Map<String, Object> featureMap) throws Exception {
        int n = dataRedisService.getNumAngryOfFriend(listreaction, friendFid);
        featureMap.put("angry_count", n);
    }

    private void addNumOfReactionFeature(String friendFid, List<Map<String, Object>> listreaction, Map<String, Object> featureMap) throws Exception {
        int n = dataRedisService.getTotalReactionOfFriend(listreaction, friendFid);
        featureMap.put("reaction_count", n);
    }

    private void addNumOfCommentFeature(String friendFid, List<Map<String, Object>> listcmts, Map<String, Object> featureMap) throws Exception {
        int n = dataRedisService.getNumCommentsOfFriend(listcmts, friendFid);
        featureMap.put("comment_count", n);
    }

    public void callApiSearchFb(String fid) {
        HttpApi httpApi = HttpApi.getInstance();
        ContentResponse response = httpApi.post(urlSearchFb, fid);
        System.out.println(response.getContentAsString());
    }

    public void callApiSearchFriends(String fid) {
        HttpApi httpApi = HttpApi.getInstance();
        ContentResponse response = httpApi.post(urlSearchFriends, fid);
        System.out.println(response.getContentAsString());
    }
}
