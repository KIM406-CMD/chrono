package com.ncist.mapper;

import com.ncist.entity.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserMapper {
    @Select("select * from user where username = #{username}")
    User selectByUsername(String username);

    @Select("select * from user where phone = #{phone}")
    User selectByPhone(String phone);

    @Insert("insert into user (username, password, phone, email, role, nickname, status, created_at, updated_at) " +
            "values (#{username}, #{password}, #{phone}, #{email}, #{role}, #{nickname}, #{status}, #{createdAt}, #{updatedAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(User user);
}