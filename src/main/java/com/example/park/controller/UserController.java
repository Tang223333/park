package com.example.park.controller;

import com.example.park.pojo.User;
import com.example.park.response.ResponseResult;
import com.example.park.service.impl.UserService;
import com.example.park.util.TextUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    UserService userService;

    /**
     * 创建管理员账号
     * @param user
     * @param request
     * @return
     */
    @PostMapping(value = "/admin_account")
    public ResponseResult initAdminAccount(@RequestBody User user, HttpServletRequest request) {
        return userService.initAdminAccount(user, request);
    }

    /**
     * 注册
     *
     * @param user
     * @return
     */
    @PostMapping(value = "/register")
    public ResponseResult register(@RequestBody User user,
                                   @RequestParam(value = "emailCode")String emailCode,
                                   @RequestParam(value = "captchaCode")String captchaCode,
                                   HttpServletRequest request) {
        return userService.register(user,emailCode,captchaCode,request);
    }

    /**
     * 密码登录
     *
     * @param captchaCode
     * @param user
     * @return
     */
    @PostMapping(value = "/login/captcha")
    public ResponseResult login(@RequestParam(value = "captchaCode") String captchaCode,
                                @RequestBody User user,
                                HttpServletRequest request) {
        return userService.loginToPassword(captchaCode,user,request);
    }

    /**
     * 邮箱验证码登录
     *
     * @param email
     * @param emailCode
     * @return
     */
    @PostMapping(value = "/login/email")
    public ResponseResult loginToEmail(@RequestParam(value = "email") String email,
                                       @RequestParam(value = "emailCode") String emailCode,
                                HttpServletRequest request) {
        return userService.loginToEmail(email,emailCode,request);
    }

    /**
     * 获取图灵验证码
     * @return
     */
    @GetMapping(value = "/captcha")
    public void getCaptcha(HttpServletResponse response,HttpServletRequest request) throws Exception {
        userService.getCaptcha(response,request);
    }

    /**
     * 发送邮件
     *
     * @param email
     * @return
     */
    @GetMapping(value = "/verify_code")
    public ResponseResult sendEmail(HttpServletRequest request,
                                    @RequestParam(value = "email") String email,
                                    @RequestParam(value = "type") String type) {
        log.info("emial => " + email);
        return userService.sendEmail(request,email,type);
    }

    /**
     * 修改密码
     *
     * @param user
     * @return
     */
    @PutMapping(value = "/password")
    public ResponseResult updatePassword(@RequestBody User user,HttpServletRequest request) {
        return userService.updatePassword(user,request);
    }

    /**
     * 获取用户信息
     * @return
     */
    @GetMapping(value = "")
    public ResponseResult getUserInfo(HttpServletRequest request) {
        return userService.findByMe(request);
    }

    /**
     * 修改用户信息
     *
     * @param user
     * @return
     */
    @PutMapping(value = "")
    public ResponseResult updateUserInfo( @RequestBody User user,HttpServletRequest request) {
        return userService.updateUser(user,request);
    }

    /**
     * 删除用户
     *
     * @return
     */
    @DeleteMapping(value = "/{userId}")
    public ResponseResult deleteUser(@PathVariable(value = "userId") String userId,HttpServletRequest request) {
        return userService.deleteBuId(userId,request);
    }

    /**
     * 获取用户列表 list
     *
     * @return
     */
    @GetMapping(value = "/list")
    public ResponseResult getUserList(HttpServletRequest request) {
        return userService.getUserList(request);
    }
}
