package com.ncist.mapper;

import com.ncist.entity.Address;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 收货地址数据访问
 */
@Mapper
public interface AddressMapper {

    @Select("SELECT id, user_id AS userId, receiver_name AS receiverName, phone, "
            + "province, city, district, detail, is_default AS isDefault, created_at AS createdAt "
            + "FROM address WHERE user_id = #{userId} ORDER BY is_default DESC, id DESC")
    List<Address> selectByUserId(Long userId);

    @Select("SELECT id, user_id AS userId, receiver_name AS receiverName, phone, "
            + "province, city, district, detail, is_default AS isDefault, created_at AS createdAt "
            + "FROM address WHERE id = #{id}")
    Address selectById(Long id);

    // 统计用户地址数量（上限校验）
    @Select("SELECT COUNT(*) FROM address WHERE user_id = #{userId}")
    int countByUserId(Long userId);

    @Insert("INSERT INTO address (user_id, receiver_name, phone, province, city, district, detail, is_default) "
            + "VALUES (#{userId}, #{receiverName}, #{phone}, #{province}, #{city}, #{district}, #{detail}, #{isDefault})")
    @org.apache.ibatis.annotations.Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Address address);

    // 动态更新（仅更新传入字段）
    @Update("<script> UPDATE address "
            + "<set> "
            + "<if test='receiverName != null'>receiver_name = #{receiverName}, </if>"
            + "<if test='phone != null'>phone = #{phone}, </if>"
            + "<if test='province != null'>province = #{province}, </if>"
            + "<if test='city != null'>city = #{city}, </if>"
            + "<if test='district != null'>district = #{district}, </if>"
            + "<if test='detail != null'>detail = #{detail}, </if>"
            + "<if test='isDefault != null'>is_default = #{isDefault}</if>"
            + "</set> "
            + "WHERE id = #{id} AND user_id = #{userId} </script>")
    int update(Address address);

    // 清除用户当前默认地址（设置新默认前调用）
    @Update("UPDATE address SET is_default = 0 WHERE user_id = #{userId} AND is_default = 1")
    int clearDefault(Long userId);

    @Update("UPDATE address SET is_default = 1 WHERE id = #{id} AND user_id = #{userId}")
    int setDefault(@Param("userId") Long userId, @Param("id") Long id);

    @Delete("DELETE FROM address WHERE id = #{id} AND user_id = #{userId}")
    int deleteById(@Param("userId") Long userId, @Param("id") Long id);
}
