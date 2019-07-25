package com.tima.top.friends.common.body.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Data
@ToString
public class TopFriendResultResponse {
    @ApiModelProperty(value = "Mã tham chiếu yêu cầu tra cứu")
    private String referenceCode;
    @ApiModelProperty(value = "Trạng thái yêu cầu tra cứu")
    private ResponseBodyObject.Status status;
    @ApiModelProperty(value = "Chi tiết trạng thái yêu cầu")
    private String statusDes;
    @ApiModelProperty(value = "Kết quả yêu cầu tra cứu")
    @JsonProperty("topFriends")
    private List<String> details = Collections.emptyList();

    @ApiModelProperty(value = "Facebook family")
    @JsonProperty("fbFamily")
    private List<Map<String, String>> familyFb = Collections.emptyList();

    @ApiModelProperty(value = "phone family")
    @JsonProperty("phoneFamily")
    Map<String, String> familyPhone = Collections.emptyMap();

    public TopFriendResultResponse(ResponseBodyObject.Status status, String statusDes, List<String> details) {
        this.status = status;
        this.statusDes = statusDes;
        this.details = details;
    }

    public TopFriendResultResponse() {
    }
}
