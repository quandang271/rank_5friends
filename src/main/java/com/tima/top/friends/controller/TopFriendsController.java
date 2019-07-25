package com.tima.top.friends.controller;

import com.tima.top.friends.common.body.request.TopFriendsRequest;
import com.tima.top.friends.common.body.response.ResponseBodyObject;
import com.tima.top.friends.common.body.response.TopFriendResultResponse;
import com.tima.top.friends.common.body.response.TopFriendsResponse;
import com.tima.top.friends.service.TopFriendsService;
import com.tima.top.friends.storage.redis.repository.DataRedisRepository;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/top-friends")
public class TopFriendsController {
    public static Logger LOG = LoggerFactory.getLogger(TopFriendsController.class);

    @Autowired
    TopFriendsService topFriendsService;

    @Autowired
    DataRedisRepository dataRedisRepository;

    @GetMapping(path = "/{referenceCode}")
    @ApiOperation(value = "Get list top friends by reference code")
    public @ResponseBody
    ResponseEntity<TopFriendsResponse> getTopFriendsByReferenceCode(@PathVariable String referenceCode){
        TopFriendsResponse response= topFriendsService.getTopFriendsByReferenceCode(referenceCode);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping(path = "/")
    @ApiOperation(value = "get list top friends")
    public @ResponseBody ResponseEntity<TopFriendsResponse> getTopFriends(@RequestBody TopFriendsRequest request){
        String phone = topFriendsService.getPhoneByUid(request.getUid());
        String fid="";
        if(phone != null && phone !=""){
            fid = dataRedisRepository.getFidByPhone(phone);
        }
        TopFriendsResponse response = new TopFriendsResponse();
        response.setResponseCode(0);
        response.setMess("Thành công");
        TopFriendResultResponse result;

        if(fid == null || fid.equals("")){
            result = new TopFriendResultResponse(ResponseBodyObject.StatusDefaultNotFound.STATUS,
                    ResponseBodyObject.StatusDefaultNotFound.STATUS_DES, null);
            response.setResult(result);
            LOG.info("fid not found for uid {} , phone {}", request.getUid(), phone);
        }else {
            LOG.info("fid {} found for uid {} , phone {}", fid, request.getUid(), phone);
            response= topFriendsService.getTopFriends(request, fid);
        }
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
