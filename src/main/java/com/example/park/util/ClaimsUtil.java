package com.example.park.util;

import com.example.park.pojo.User;
import io.jsonwebtoken.Claims;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * claims token 处理
 */
public class ClaimsUtil {

    public static final String ID = "id";

    public static Map<String,Object> myUser2Claims(User myUser){
        Map<String,Object> claims=new HashMap<>();
        claims.put(ID,myUser.getId());
        return claims;
    }

    public static User claims2MyUser(Claims claims){
        User myUser=new User();
        String userId= (String) claims.get(ID);
        myUser.setId(userId);
        return myUser;
    }
}
