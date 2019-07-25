package com.tima.top.friends.common.body.response;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;

import java.util.Collections;
import java.util.List;

@Data
@ToString
public class TopFriendsResponse implements ResponseBodyObject {

    @ApiModelProperty(value = "mã lỗi, 0: Success, 1: Error ")
    protected int responseCode;
    @ApiModelProperty(value = "Mô tả lỗi")
    protected String mess;

    protected TopFriendResultResponse result;

    public int getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(int responseCode) {
        this.responseCode = responseCode;
    }

    public String getMess() {
        return mess;
    }

    public void setMess(String mess) {
        this.mess = mess;
    }

    public TopFriendResultResponse getResult() {
        return result;
    }

    public void setResult(TopFriendResultResponse result) {
        this.result = result;
    }
}
