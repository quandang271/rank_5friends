package com.tima.top.friends.common.body.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class TopFriendsRequest implements RequestBodyObject {
    @ApiModelProperty(value = "Mã khách hàng")
    @JsonProperty("uid")
    protected String uid;

}
