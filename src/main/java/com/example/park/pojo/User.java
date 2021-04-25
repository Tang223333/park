package com.example.park.pojo;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;

@Getter
@Setter
@ToString
public class User {
    private String id;
    private String userName;
    private String password;
    private String newPas;
    private String phone;
    private String roles;
    private String avatar;
    private String email;
    private String sign;
    private String state;
    private String regIp;
    private String loginIp;
    private Date createTime;
    private Date updateTime;
    private String emailCode;
}
