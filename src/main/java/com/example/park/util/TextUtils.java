package com.example.park.util;

import javax.servlet.http.HttpServletRequest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * text工具类
 */
public class TextUtils {

    public static final String regEx = "^([a-z0-9A-Z]+[-|\\.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$";

    public static boolean isEmpty(String string){
        return null==string||"null".equals(string)||string.length()==0;
    }

    public static boolean isEmailSuccess(String email){
        Pattern p = Pattern.compile(regEx);
        Matcher m = p.matcher(email);
        return m.matches();
    }

    public static String getKey(HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");
        if (TextUtils.isEmpty(authorization)) return "";
        authorization = authorization.replace("Bearer ","");
        return authorization;
    }

    public static String getRemoteAddr(HttpServletRequest request){
        String remoteAddr = request.getRemoteAddr();
        if (null!=remoteAddr){
            remoteAddr=remoteAddr.replaceAll(":","_");
        }
        return remoteAddr;
    }
}
