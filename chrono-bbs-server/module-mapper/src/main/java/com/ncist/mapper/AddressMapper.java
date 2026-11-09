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
 * 收货地址数据访问。
 *
 * <p>所有写操作均以 {@code userId} 限定属主，避免越权修改他人地址；
 * 默认地址的切换需先清除旧默认再设置新默认。</p>
 */
@Mapper
public interface AddressMapper {

    /**
     * 查询用户的全部收货地址，默认地址优先，其次按 ID 倒序。
     *
     * @param userId 用户 ID，通常为当前登录用户
     * @return 地址列表，无记录时返回空列表
     */
    @Select("SELECT id, user_id AS userId, receiver_name AS receiverName, phone, "
            + "province, city, district, detail, is_default AS isDefault, created_at AS createdAt "
            + "FROM address WHERE user_id = #{userId} ORDER BY is_default DESC, id DESC")
    List<Address> selectByUserId(Long userId);

    /**
     * 按主键查询地址。
     *
     * <p>不限定属主，调用方须自行校验地址归属。</p>
     *
     * @param id 地址 ID
     * @return 地址记录，不存在时返回 null
     */
    @Select("SELECT id, user_id AS userId, receiver_name AS receiverName, phone, "
            + "province, city, district, detail, is_default AS isDefault, created_at AS createdAt "
            + "FROM address WHERE id = #{id}")
    Address selectById(Long id);

    /**
     * 统计用户已保存的地址数量，用于地址上限校验。
     *
     * @param userId 用户 ID
     * @return 该用户的地址条数
     */
    @Select("SELECT COUNT(*) FROM address WHERE user_id = #{userId}")
    int countByUserId(Long userId);

    /**
     * 新增收货地址，并回填自增主键。
     *
     * @param address 地址信息，userId 必须为当前登录用户
     * @return 受影响行数
     */
    @Insert("INSERT INTO address (user_id, receiver_name, phone, province, city, district, detail, is_default) "
            + "VALUES (#{userId}, #{receiverName}, #{phone}, #{province}, #{city}, #{district}, #{detail}, #{isDefault})")
    @org.apache.ibatis.annotations.Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Address address);

    /**
     * 动态更新地址，仅更新非 null 字段；SQL 以 {@code userId} 限定属主。
     *
     * @param address 待更新地址，id 与 userId 必填，其余字段为 null 表示不修改
     * @return 受影响行数，0 表示地址不存在或不属于该用户
     */
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

    /**
     * 清除用户当前的默认地址。
     *
     * <p>设置新默认地址前调用，保证同一用户至多一个默认地址。</p>
     *
     * @param userId 用户 ID
     * @return 受影响行数
     */
    @Update("UPDATE address SET is_default = 0 WHERE user_id = #{userId} AND is_default = 1")
    int clearDefault(Long userId);

    /**
     * 将指定地址设为默认地址。
     *
     * @param userId 用户 ID，用于属主校验
     * @param id 地址 ID
     * @return 受影响行数，0 表示地址不存在或不属于该用户
     */
    @Update("UPDATE address SET is_default = 1 WHERE id = #{id} AND user_id = #{userId}")
    int setDefault(@Param("userId") Long userId, @Param("id") Long id);

    /**
     * 删除指定地址，限定属主。
     *
     * @param userId 用户 ID
     * @param id 地址 ID
     * @return 受影响行数，0 表示地址不存在或不属于该用户
     */
    @Delete("DELETE FROM address WHERE id = #{id} AND user_id = #{userId}")
    int deleteById(@Param("userId") Long userId, @Param("id") Long id);
}
