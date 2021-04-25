package com.example.park.service.impl;

import com.example.park.dao.IRequestTokenDao;
import com.example.park.dao.IUserDao;
import com.example.park.pojo.RequestToken;
import com.example.park.pojo.User;
import com.example.park.response.ResponseResult;
import com.example.park.service.IUserService;
import com.example.park.util.*;
import com.wf.captcha.ArithmeticCaptcha;
import com.wf.captcha.GifCaptcha;
import com.wf.captcha.SpecCaptcha;
import com.wf.captcha.base.Captcha;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Slf4j
@Service
public class UserService implements IUserService {

    @Autowired
    IUserDao iUserDao;

    @Autowired
    IRequestTokenDao iRequestTokenDao;

    @Autowired
    Random random;

    @Autowired
    RedisUtil redisUtil;

    @Autowired
    TaskService taskService;

    @Autowired
    BCryptPasswordEncoder cryptPasswordEncoder;

    //图灵验证码的样式
    private static final int[] captchaFont = { Captcha.FONT_1
            ,Captcha.FONT_2
            ,Captcha.FONT_3
            ,Captcha.FONT_4
            ,Captcha.FONT_5
            ,Captcha.FONT_6
            ,Captcha.FONT_7
            ,Captcha.FONT_8
            ,Captcha.FONT_9
            ,Captcha.FONT_10};
    //图灵验证码的类型
    private static final int[] captchaCharType = { Captcha.TYPE_DEFAULT
            ,Captcha.TYPE_ONLY_NUMBER
            ,Captcha.TYPE_ONLY_CHAR
            ,Captcha.TYPE_ONLY_UPPER
            ,Captcha.TYPE_ONLY_LOWER
            ,Captcha.TYPE_NUM_AND_UPPER};

    //邮箱验证码的内容
    private static final char[] emailCodes = {'0','1','2','3','4','5','6','7','8','9',
            'a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p','q','r','s','t','u','v','w','x','y','z',
            'A','B','C','D','E','F','G','H','I','J','K','L','M','N','O','P','Q','R','S','T','U','V','W','X','Y','Z'};

    @Override
    public ResponseResult initAdminAccount(User user, HttpServletRequest request) {
        if (user.getUserName().isEmpty()) return ResponseResult.DEFAULT("用户名不能为空!").setData(null);
        if (user.getPassword().isEmpty()) return ResponseResult.DEFAULT("密码不能为空!").setData(null);
        if (user.getEmail().isEmpty()) return ResponseResult.DEFAULT("邮箱不能为空!").setData(null);
        if (user.getPhone().isEmpty())return ResponseResult.DEFAULT("手机号不能为空!").setData(null);

        User userDB = iUserDao.findMyUserByUserName(user.getUserName());
        if (null!=userDB)return ResponseResult.DEFAULT("用户名已存在").setData(null);
        userDB=iUserDao.findMyUserByEmail(user.getEmail());
        if (null!=userDB) return ResponseResult.DEFAULT("邮箱已注册").setData(null);
        userDB=iUserDao.findMyUserByPhone(user.getPhone());
        if (null!=userDB) return ResponseResult.DEFAULT("手机号已注册").setData(null);
        //获取原密码
        String password = user.getPassword();
        //加密
        password=cryptPasswordEncoder.encode(password);
        user.setPassword(password);
        String key =UUIDWorker.getId();
        user.setId(key);
        user.setAvatar(Constants.USER.USER_DEFAULT_IMAGE_URL);
        user.setRoles(Constants.USER.USER_DEFAULT_ROLES);
        user.setSign(Constants.USER.USER_DEFAULT_SIGN);
        user.setState("2");
        user.setRegIp(request.getLocalAddr());
        user.setLoginIp(request.getRemoteAddr());
        user.setCreateTime(new Date());
        user.setUpdateTime(new Date());
        iUserDao.save(user);
        return ResponseResult.SUCCESS("管理员账号创建成功").setData(user);
    }

    @Override
    public void getCaptcha(HttpServletResponse response, HttpServletRequest request) throws Exception {
        // 设置请求头为输出图片类型
        response.setContentType("image/gif");
        response.setHeader("Pragma", "No-cache");
        response.setHeader("Cache-Control", "no-cache");
        response.setDateHeader("Expires", 0);
        int captchaType=random.nextInt(3);
        Captcha captcha=null;
        if (captchaType==0){
            // 三个参数分别为宽、高、位数
            captcha = new SpecCaptcha(130, 48, 5);
        }else if (captchaType==1){
            // gif类型
            captcha = new GifCaptcha(130, 48);
        }else {
            // 算术类型
            captcha = new ArithmeticCaptcha(130, 48);
            captcha.setLen(3);  // 几位数运算，默认是两位
            // ((ArithmeticCaptcha)captcha).getArithmeticString();  // 获取运算的公式：3+2=?
        }
        // 设置字体
        captcha.setFont(captchaFont[random.nextInt(captchaFont.length)]);  // 有默认字体，可以不用设置
        // 设置类型，纯数字、纯字母、字母数字混合
        captcha.setCharType(captchaCharType[random.nextInt(captchaCharType.length)]);

        String value = captcha.text();// 获取验证码

        String remoteAddr = request.getRemoteAddr();
        if (remoteAddr != null) {
            remoteAddr=remoteAddr.replaceAll(":","_");
        }

        redisUtil.set(Constants.USER.USER_CAPTCHA_CONTENT+remoteAddr,value,60*10);

        // 验证码存入session
        request.getSession().setAttribute("captcha", value);

        // 输出图片流
        captcha.out(response.getOutputStream());
    }

    /**
     * 判断类型：
     * 1.注册 type=register，判断邮箱是否已注册
     * 2.登录 type=login，判断邮箱是否注册
     * 3.修改邮箱 type=updateEmail，判断邮箱是否已注册
     * @param request
     * @param email
     * @return
     */
    @Override
    public ResponseResult sendEmail(HttpServletRequest request, String email, String type) {
        //1.判断邮箱是否正确
        if (TextUtils.isEmpty(email)) return ResponseResult.DEFAULT("邮箱地址不能为空!");
        if (!TextUtils.isEmailSuccess(email)) return ResponseResult.DEFAULT("邮箱格式不正确!");
        //2.判断邮箱是否可用（1小时内只可对1个邮箱发送10次邮件，30秒内只能发送1次邮件）
        User myUserByEmail = iUserDao.findMyUserByEmail(email);
        if ("login".equals(type)){
            if (null==myUserByEmail) return ResponseResult.DEFAULT("该邮箱未注册");
        }else {
            if (null!=myUserByEmail) return ResponseResult.DEFAULT("该邮箱已注册");
        }
        String remoteAddr =TextUtils.getRemoteAddr(request);
        Object o = redisUtil.get(Constants.USER.USER_EMAIL_SEND_IP + remoteAddr);
        if (null!=o) return ResponseResult.DEFAULT("您发送邮件过快，请稍后再试!");
        Integer sendIpAccount = (Integer) redisUtil.get((Constants.USER.USER_EMAIL_SEND_IP_ACCOUNT + remoteAddr));
        if (null==sendIpAccount) sendIpAccount=0;
        if (sendIpAccount>10) return ResponseResult.DEFAULT("您发送邮件次数过多，请稍后再试!");
        //3.发送邮件并返回结果
        StringBuffer emailCode=new StringBuffer();
        for (int i = 0; i < 6; i++) {
            emailCode.append(emailCodes[random.nextInt(emailCodes.length)]);
        }
        try {
            taskService.sendEmailCode(email,emailCode.toString());
        }catch (Exception e){
            return ResponseResult.DEFAULT("邮件发送失败，请稍后再试！");
        }
        redisUtil.set(Constants.USER.USER_EMAIL_SEND_IP+remoteAddr,"true",30);
        sendIpAccount++;
        redisUtil.set(Constants.USER.USER_EMAIL_SEND_IP_ACCOUNT+remoteAddr,sendIpAccount,60*60);
        redisUtil.set(Constants.USER.USER_EMAIL_CONTENT+remoteAddr+email,emailCode,60*10);
        log.info("email code => "+emailCode.toString());
        return ResponseResult.SUCCESS("邮件发送成功!");
    }

    @Override
    public ResponseResult register(User user, String emailCode, String captchaCode, HttpServletRequest request) {
        //1.判断用户名，密码，邮箱,手机号，验证码是否输入
        String userName=user.getUserName();
        if (TextUtils.isEmpty(userName)) return ResponseResult.DEFAULT("用户名不能为空!");
        String password=user.getPassword();
        if (TextUtils.isEmpty(password)) return ResponseResult.DEFAULT("密码不能为空!");
        String email=user.getEmail();
        if (TextUtils.isEmpty(email)) return ResponseResult.DEFAULT("邮箱不能为空!");
        String phone=user.getPhone();
        if (TextUtils.isEmpty(phone)) return ResponseResult.DEFAULT("手机号不能为空!");
        if (TextUtils.isEmpty(captchaCode)) return ResponseResult.DEFAULT("人类验证码不能为空!");
        if (TextUtils.isEmpty(emailCode)) return ResponseResult.DEFAULT("邮箱验证码不能为空!");
        //2.判断邮箱格式是否正确
        if (!TextUtils.isEmailSuccess(email)) return ResponseResult.DEFAULT("邮箱已注册!");
        //3.判断用户名是否已注册，邮箱是否已绑定
        User myUser=iUserDao.findMyUserByUserName(userName);
        if (null!=myUser) return ResponseResult.DEFAULT("用户名已注册!");
        myUser=iUserDao.findMyUserByEmail(email);
        if (null!=myUser) return ResponseResult.DEFAULT("邮箱已注册!");
        myUser=iUserDao.findMyUserByPhone(phone);
        if (null!=myUser) return ResponseResult.DEFAULT("手机号已注册!");
        //4.判断邮箱验证码是否正确
        String remoteAddr = TextUtils.getRemoteAddr(request);
        String eCode = String.valueOf(redisUtil.get(Constants.USER.USER_EMAIL_CONTENT+ remoteAddr+email).toString());
        if (TextUtils.isEmpty(eCode)) return ResponseResult.DEFAULT("邮箱错误或邮箱验证码已失效!");
        if (!emailCode.equals(eCode)) return ResponseResult.DEFAULT("邮箱验证码错误！");
        //5.判断图灵验证码是否正确
        String cCode = String.valueOf(redisUtil.get(Constants.USER.USER_CAPTCHA_CONTENT+remoteAddr));
        if (TextUtils.isEmpty(cCode)) return ResponseResult.DEFAULT("人类验证码已失效!");
        if (!captchaCode.equals(cCode)) return ResponseResult.DEFAULT("人类验证码错误!");
        //进入可注册状态
        //6.密码加密
        password=cryptPasswordEncoder.encode(password);
        user.setPassword(password);
        //7.补充数据
        user.setId(UUIDWorker.getId());
        user.setAvatar(Constants.USER.USER_DEFAULT_IMAGE_URL);
        user.setRoles(Constants.USER.USER_DEFAULT_ROLES2);
        user.setSign(Constants.USER.USER_DEFAULT_SIGN);
        user.setState("1");
        user.setRegIp(request.getLocalAddr());
        user.setLoginIp(request.getRemoteAddr());
        user.setCreateTime(new Date());
        user.setUpdateTime(new Date());
        //8.加入数据库
        iUserDao.save(user);
        //9.返回结果
        return ResponseResult.SUCCESS("注册成功!");
    }

    @Override
    public ResponseResult loginToPassword(String captchaCode, User user, HttpServletRequest request) {
        //1.判断用户名，密码，验证码是否输入
        String userName=user.getUserName();
        if (TextUtils.isEmpty(userName)) return ResponseResult.DEFAULT("用户名不能为空!");
        String password=user.getPassword();
        if (TextUtils.isEmpty(password)) return ResponseResult.DEFAULT("密码不能为空!");
        if (TextUtils.isEmpty(captchaCode)) return ResponseResult.DEFAULT("验证码不能为空!");
        //2.判断用户名，是否存在
        User userDb = iUserDao.findMyUserByUserName(userName);
        if (null==userDb) return ResponseResult.DEFAULT("用户名或密码错误!");
        //3.判断验证码是否正确
        String remoteAddr = TextUtils.getRemoteAddr(request);
        String cCode =(String) redisUtil.get(Constants.USER.USER_CAPTCHA_CONTENT + remoteAddr);
        if (TextUtils.isEmpty(cCode)) return ResponseResult.DEFAULT("验证码已失效!");
        if (!captchaCode.equals(cCode)) return ResponseResult.DEFAULT("验证码错误!");
        //4.判断密码是否正确
        boolean matches = cryptPasswordEncoder.matches(password, userDb.getPassword());
        if (!matches) return ResponseResult.DEFAULT("用户名或密码错误!");
        //5.判断该账号是否可用
        if (userDb.getState().equals("0")) return ResponseResult.DEFAULT("当前账号不可用!");
        //符合登录条件
        return loginReturn(userDb);
    }

    @Override
    public ResponseResult loginToEmail(String email,String emailCode, HttpServletRequest request) {
        //1.判断邮箱，验证码是否输入
        if (TextUtils.isEmpty(email)) return ResponseResult.DEFAULT("邮箱不能为空!");
        if (TextUtils.isEmpty(emailCode)) return ResponseResult.DEFAULT("验证码不能为为空!");
        //2.判断用户名，是否存在
        User userDb = iUserDao.findMyUserByEmail(email);
        if (null==userDb) return ResponseResult.DEFAULT("该邮箱尚未注册");
        //3.判断验证码是否正确
        String remoteAddr = TextUtils.getRemoteAddr(request);
        String eCode =(redisUtil.get(Constants.USER.USER_EMAIL_CONTENT+remoteAddr+email).toString());
        if (TextUtils.isEmpty(eCode)) return ResponseResult.DEFAULT("验证码已失效!");
        if (!emailCode.equals(eCode)) return ResponseResult.DEFAULT("验证码错误!");

        //5.判断该账号是否可用
        if (userDb.getState().equals("0")) return ResponseResult.DEFAULT("当前账号不可用!");

        //符合登录条件
        return loginReturn(userDb);
    }

    private ResponseResult loginReturn(User userDb) {
        //6.生成token
        Map<String,Object> claims=ClaimsUtil.myUser2Claims(userDb);
        String token = JwtUtil.createToken(claims);
        //将token进行MD5加密得到tokenKey
        String tokenKey = DigestUtils.appendMd5DigestAsHex(token.getBytes(), new StringBuilder()).toString();
        //将tokenKey存入redis中
        redisUtil.set(tokenKey,token,Constants.TIME.HOURS_2);
        //7.生成requestToken。
        String refreshToken = JwtUtil.createRefreshToken(userDb.getId(), Constants.TIME.MONTH);
        //创建表 request_token 字段 id,user_id,token_key,update_time,create_time
        RequestToken requestToken=iRequestTokenDao.findByUserId(userDb.getId());
        boolean isNull=(null==requestToken);
        log.info(isNull+"");
        if (isNull){
            requestToken=new RequestToken();
            requestToken.setId(UUIDWorker.getId());
            requestToken.setUserId(userDb.getId());
            requestToken.setCreateTime(new Date());
        }
        requestToken.setRequestToken(refreshToken);
        requestToken.setUpdateTime(new Date());
        requestToken.setTokenKey(tokenKey);
        if (isNull){
            iRequestTokenDao.save(requestToken);
        }else {
            iRequestTokenDao.update(requestToken);
        }
        //7.返回结果
        token=tokenKey;
        return ResponseResult.SUCCESS("登录成功!").setData(token);
    }

    @Override
    public String requestToken(String tokenKey) {
        String token="";
        //判断requestToken是否存在
        RequestToken requestToken = iRequestTokenDao.findRequestTokenByTokenKey(tokenKey);
        if (null == requestToken) return "";
        //requestToken存在，重新生成，token与requestToken
        try {
            Claims claims = JwtUtil.parseJWT(requestToken.getRequestToken());
            User myUser = iUserDao.findMyUserById(claims.getId());
            Map<String, Object> map = ClaimsUtil.myUser2Claims(myUser);
            token = JwtUtil.createToken(map);
            redisUtil.set(tokenKey, token, Constants.TIME.HOURS_2);
            requestToken.setUpdateTime(new Date());
            iRequestTokenDao.update(requestToken);
        } catch (Exception e) {
            //requestToken已过期
            return "";
        }
        return token;
    }

    @Override
    public ResponseResult updatePassword(User user, HttpServletRequest request) {
        if (TextUtils.isEmpty(user.getId()))return ResponseResult.DEFAULT("用户ID不能为空!");
        if (TextUtils.isEmpty(user.getPassword()))return ResponseResult.DEFAULT("旧密码不能为空!");
        if (TextUtils.isEmpty(user.getNewPas()))return ResponseResult.DEFAULT("新密码不能为空!");
        //获取token的key
        String key = TextUtils.getKey(request);
        if (TextUtils.isEmpty(key))return ResponseResult.DEFAULT("认证失败!");
        //通过key获取token
        String token = (String) redisUtil.get(key);
        if (TextUtils.isEmpty(token)){
            token=requestToken(key);
            if (TextUtils.isEmpty(token)) return ResponseResult.DEFAULT("认证失败!");
        }
        Claims claims = JwtUtil.parseJWT(token);
        User myUser= ClaimsUtil.claims2MyUser(claims);
        if (!myUser.getId().equals(user.getId())){
            return ResponseResult.DEFAULT("用户ID有错!");
        }
        myUser=iUserDao.findMyUserById(user.getId());
        if (null==myUser)return ResponseResult.DEFAULT("该用户不存在!");
        //密码判断
        boolean matches = cryptPasswordEncoder.matches(user.getPassword(),myUser.getPassword());
        if (!matches)return ResponseResult.DEFAULT("原密码有误!");
        //修改数据
        user.setPassword(cryptPasswordEncoder.encode(user.getNewPas()));
        iUserDao.updateUserPas(user);
        redisUtil.del(key);
        iRequestTokenDao.removeByUserId(user.getId());
        return ResponseResult.SUCCESS("修改成功!");
    }

    private String getToken(String key) {
        String token  = (String) redisUtil.get(key);
        if (TextUtils.isEmpty(token)){
            token=requestToken(key);
            if (TextUtils.isEmpty(token))
                return "";
        }
        return token;
    }

    @Override
    public ResponseResult findByMe(HttpServletRequest request) {
        //获取token的key
        String key = TextUtils.getKey(request);
        if (TextUtils.isEmpty(key)) return ResponseResult.DEFAULT("认证失败!");
        //通过key获取token
        String token = (String) redisUtil.get(key);
        if (TextUtils.isEmpty(token)){
            token=requestToken(key);
            if (TextUtils.isEmpty(token)) return ResponseResult.DEFAULT("认证失败!");
        }
        Claims claims = JwtUtil.parseJWT(token);
        User myUser=ClaimsUtil.claims2MyUser(claims);
        myUser=iUserDao.findMyUserById(myUser.getId());
        return ResponseResult.SUCCESS("查询成功").setData(myUser);
    }

    @Override
    public ResponseResult updateUser(User user, HttpServletRequest request) {
        if (TextUtils.isEmpty(user.getId()))return ResponseResult.DEFAULT("用户名ID不能为空");
        //获取token的key
        String key = TextUtils.getKey(request);
        if (TextUtils.isEmpty(key)) return ResponseResult.DEFAULT("认证失败!");
        //通过key获取token
        String token = (String) redisUtil.get(key);
        if (TextUtils.isEmpty(token)){
            token=requestToken(key);
            if (TextUtils.isEmpty(token)) return ResponseResult.DEFAULT("认证失败!");
        }
        Claims claims = JwtUtil.parseJWT(token);
        User myUser=ClaimsUtil.claims2MyUser(claims);
        if (!myUser.getId().equals(user.getId())) return ResponseResult.DEFAULT("用户ID有误!");
        myUser=iUserDao.findMyUserById(myUser.getId());
        myUser.setUserName(TextUtils.isEmpty(user.getUserName())?myUser.getUserName():user.getUserName());
        myUser.setAvatar(TextUtils.isEmpty(user.getAvatar())?myUser.getAvatar():user.getAvatar());
        myUser.setSign(TextUtils.isEmpty(user.getSign())?myUser.getUserName():user.getSign());
        if (!TextUtils.isEmpty(user.getEmail())){
            //邮箱验证
            User u=iUserDao.findMyUserByEmail(user.getEmail());
            if (null!=u)return ResponseResult.DEFAULT("改邮箱已注册!");
            String remoteAddr = TextUtils.getRemoteAddr(request);
            if (TextUtils.isEmpty(user.getEmailCode()))return ResponseResult.DEFAULT("验证码不能为空!");
            String eCode =(redisUtil.get(Constants.USER.USER_EMAIL_CONTENT+remoteAddr+user.getEmail()).toString());
            if (TextUtils.isEmpty(eCode)) return ResponseResult.DEFAULT("验证码已失效!");
            if (!user.getEmailCode().equals(eCode)) return ResponseResult.DEFAULT("验证码错误!");
            myUser.setEmail(user.getEmail());
        }else {
            myUser.setEmail(myUser.getEmail());
        }
        if (!TextUtils.isEmpty(user.getPhone())){
            //电话号验证
            User u=iUserDao.findMyUserByPhone(user.getPhone());
            if (null!=u)return ResponseResult.DEFAULT("手机号已注册!");
            myUser.setPhone(user.getPhone());
        }else {
            myUser.setPhone(myUser.getPhone());
        }
        myUser.setUpdateTime(new Date());
        iUserDao.updateUser(myUser);
        redisUtil.del(key);
        iRequestTokenDao.removeByUserId(myUser.getId());
        return ResponseResult.SUCCESS("修改成功,请重新登录");
    }

    @Override
    public ResponseResult deleteBuId(String userId,HttpServletRequest request) {
        if (TextUtils.isEmpty(userId))return ResponseResult.DEFAULT("用户名ID不能为空");
        //获取token的key
        String key = TextUtils.getKey(request);
        if (TextUtils.isEmpty(key)) return ResponseResult.DEFAULT("认证失败!");
        //通过key获取token
        String token = (String) redisUtil.get(key);
        if (TextUtils.isEmpty(token)){
            token=requestToken(key);
            if (TextUtils.isEmpty(token)) return ResponseResult.DEFAULT("认证失败!");
        }
        Claims claims = JwtUtil.parseJWT(token);
        User myUser=ClaimsUtil.claims2MyUser(claims);
        if (!myUser.getId().equals(userId)) return ResponseResult.DEFAULT("用户ID有误!");
        User user=iUserDao.findMyUserById(userId);
        user.setState("0");
        iUserDao.updateUser(user);
        redisUtil.del(key);
        iRequestTokenDao.removeByUserId(user.getId());
        return ResponseResult.SUCCESS("用户删除成功");
    }

    @Override
    public ResponseResult getUserList(HttpServletRequest request) {
        //获取token的key
        String key = TextUtils.getKey(request);
        if (TextUtils.isEmpty(key)) return ResponseResult.DEFAULT("认证失败!");
        //通过key获取token
        String token = (String) redisUtil.get(key);
        if (TextUtils.isEmpty(token)){
            token=requestToken(key);
            if (TextUtils.isEmpty(token)) return ResponseResult.DEFAULT("认证失败!");
        }
        Claims claims = JwtUtil.parseJWT(token);
        User myUser=ClaimsUtil.claims2MyUser(claims);
        User user=iUserDao.findMyUserById(myUser.getId());
        if (!user.getState().equals("2"))
            return ResponseResult.DEFAULT("权限不足");
        List<User> userList = iUserDao.findUserList();
        return ResponseResult.SUCCESS("查询成功").setData(userList);
    }
}
