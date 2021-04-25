package com.example.park.pojo;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;

@Getter
@Setter
@ToString
public class RequestToken {
    private String id;
    private String requestToken;
    private String userId;
    private String tokenKey;
    private Date createTime;
    private Date updateTime;
}
