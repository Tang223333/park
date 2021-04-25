package com.example.park.dao;

import com.example.park.pojo.RequestToken;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

@Repository
public interface IRequestTokenDao {

    @Insert("insert into request_token(id,userId,requestToken,tokenKey,createTime,updateTime) values " +
            "(#{id},#{userId},#{requestToken},#{tokenKey},#{createTime},#{updateTime});")
    void save(RequestToken requestToken);

    @Select("select * from request_token where tokenKey=#{tokenKey};")
    RequestToken findRequestTokenByTokenKey(String tokenKey);

    @Update("update request_token set requestToken=#{requestToken},tokenKey=#{tokenKey},updateTime=#{updateTime} where id=#{id}")
    void update(RequestToken requestToken);

    @Select("select * from request_token where userId=#{userId};")
    RequestToken findByUserId(String userId);

    @Delete("delete from request_token where userId=#{userId};")
    void removeByUserId(String userId);
}
