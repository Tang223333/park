package com.example.park.dao;

import com.example.park.pojo.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IUserDao {

    @Select("select * from user where id=#{id}")
    User findMyUserById(String id);

    @Select("select * from user where email=#{email}")
    User findMyUserByEmail(String email);

    @Select("select * from user where phone=#{phone}")
    User findMyUserByPhone(String phone);

    @Select("select * from user where userName=#{userName}")
    User findMyUserByUserName(String userName);

    @Insert("insert into user(id,userName,password,roles,avatar,email,sign,state,phone,regIp,loginIp,createTime,updateTime) values" +
            "(#{id},#{userName},#{password},#{roles},#{avatar},#{email},#{sign},#{state},#{phone},#{regIp},#{loginIp},#{createTime},#{updateTime});")
    void save(User user);

    @Update("update user set userName=#{userName},roles=#{roles},avatar=#{avatar},email=#{email},sign=#{sign},state=#{state},phone=#{phone},regIp=#{regIp},loginIp=#{loginIp},updateTime=#{updateTime} where id=#{id}")
    void updateUser(User user);

    @Update("update user set password=#{password} where id=#{id}")
    void updateUserPas(User user);

    @Select("select * from user")
    List<User> findUserList();
}
