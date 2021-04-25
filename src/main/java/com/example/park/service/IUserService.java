package com.example.park.service;

import com.example.park.pojo.User;
import com.example.park.response.ResponseResult;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

public interface IUserService {

    ResponseResult initAdminAccount(User user, HttpServletRequest request);

    void getCaptcha(HttpServletResponse response, HttpServletRequest request) throws Exception;

    ResponseResult sendEmail(HttpServletRequest request, String verifyCodeAdd,String type);

    ResponseResult register(User user, String captchaCode, String emailCode, HttpServletRequest request);

    ResponseResult loginToPassword(String captcha, User user, HttpServletRequest request);

    ResponseResult loginToEmail(String email,String emailCode,HttpServletRequest request);

    String requestToken(String tokenKey);

    ResponseResult updatePassword(User user, HttpServletRequest request);

    ResponseResult findByMe(HttpServletRequest request);

    ResponseResult updateUser(User user, HttpServletRequest request);

    ResponseResult deleteBuId(String userId,HttpServletRequest request);

    ResponseResult getUserList(HttpServletRequest request);
}
