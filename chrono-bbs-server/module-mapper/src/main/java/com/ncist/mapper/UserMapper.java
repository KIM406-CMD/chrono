package com.ncist.mapper;

import com.ncist.entity.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface UserMapper {
    @Select("select * from user where username = #{username}")
    User selectByUsername(String username);

    @Select("select * from user where phone = #{phone}")
    User selectByPhone(String phone);

    @Select("select * from user where id = #{id}")
    User selectById(Long id);

    @Select("select * from user where phone = #{account} or email = #{account} limit 1")
    User selectByAccount(String account);

    @Insert("insert into user (username, password, phone, email, role, nickname, status, created_at, updated_at) " +
            "values (#{username}, #{password}, #{phone}, #{email}, #{role}, #{nickname}, #{status}, #{createdAt}, #{updatedAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(User user);

    @Update("update user set password = #{password}, updated_at = now() where id = #{id}")
    int updatePassword(@Param("id") Long id, @Param("password") String password);

    @Update("""
            <script>
            update user set updated_at = now()
            <if test="nickname != null and nickname != ''">, nickname = #{nickname}</if>
            <if test="phone != null and phone != ''">, phone = #{phone}</if>
            <if test="email != null and email != ''">, email = #{email}</if>
            <if test="avatar != null and avatar != ''">, avatar = #{avatar}</if>
            where id = #{userId}
            </script>
            """)
    int updateProfile(@Param("userId") Long userId,
                     @Param("nickname") String nickname,
                     @Param("phone") String phone,
                     @Param("email") String email,
                     @Param("avatar") String avatar);
}
