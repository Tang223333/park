package com.example.park.util;

public interface Constants {

    interface USER {
        String USER_DEFAULT_IMAGE_URL ="https://cdn.sunofbeaches.com/images/default_avatar.png";
        String USER_DEFAULT_ROLES ="超级管理员";
        String USER_DEFAULT_ROLES2 ="普通用户";
        String USER_DEFAULT_SIGN ="我的目标是星辰大海!";
        String USER_CAPTCHA_CONTENT ="user_captcha_content_";
        String USER_EMAIL_CONTENT = "user_email_content_";
        String USER_EMAIL_SEND_IP = "user_email_send_ip_";
        String USER_EMAIL_SEND_IP_ACCOUNT = "user_email_send_ip_account_";
        String USER_LOGIN_COOKIE_NAME="park_system";
    }

    interface SETTING{
        String SETTING_ADMIN_ACCOUNT_EXIST ="USER_ACCOUNT_EXIST";
        String SETTING_ADMIN_ACCOUNT_UN_EXIST ="USER_ACCOUNT_UN_EXIST";
        String SETTING_DEFAULT_VALUE="value";
    }

    interface TIME {
        int MIN=60;
        int HOURS=MIN*60;
        int HOURS_2=MIN*60*2;
        int DAY=HOURS*24;
        int MONTH=DAY*30;
        int MONTH_2=DAY*30*2;
        int MONTH_12=DAY*30*12;
        int YEAR=DAY*365;
    }
}
