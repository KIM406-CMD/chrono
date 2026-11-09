package com.ncist.mapper;

import com.ncist.entity.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 用户账号数据访问。
 *
 * <p>覆盖登录账号查找、注册、资料与密码修改、角色/状态调整及管理后台分页。
 * 密码字段存放 BCrypt 摘要，任何查询结果都不应直接返回给前端。</p>
 */
@Mapper
public interface UserMapper {

    /**
     * 按用户名查询用户，用于登录与用户名唯一性校验。
     *
     * @param username 登录用户名
     * @return 用户记录，不存在时返回 null
     */
    @Select("select * from user where username = #{username}")
    User selectByUsername(String username);

    /**
     * 按手机号查询用户，用于手机号登录与唯一性校验。
     *
     * @param phone 手机号
     * @return 用户记录，不存在时返回 null
     */
    @Select("select * from user where phone = #{phone}")
    User selectByPhone(String phone);

    /**
     * 按主键查询用户。
     *
     * @param id 用户 ID
     * @return 用户记录，不存在时返回 null
     */
    @Select("select * from user where id = #{id}")
    User selectById(Long id);

    /**
     * 按账号查询用户，账号可为手机号或邮箱。
     *
     * @param account 手机号或邮箱
     * @return 匹配到的用户记录，不存在时返回 null
     */
    @Select("select * from user where phone = #{account} or email = #{account} limit 1")
    User selectByAccount(String account);

    /**
     * 新增用户，并回填自增主键。
     *
     * @param user 用户信息；password 必须为 BCrypt 摘要，username 需唯一
     */
    @Insert("insert into user (username, password, phone, email, role, nickname, status, created_at, updated_at) " +
            "values (#{username}, #{password}, #{phone}, #{email}, #{role}, #{nickname}, #{status}, #{createdAt}, #{updatedAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(User user);

    /**
     * 更新用户密码。
     *
     * @param id 用户 ID
     * @param password 新的 BCrypt 密码摘要，不能为明文
     * @return 受影响行数，0 表示用户不存在
     */
    @Update("update user set password = #{password}, updated_at = now() where id = #{id}")
    int updatePassword(@Param("id") Long id, @Param("password") String password);

    /**
     * 动态更新用户资料，仅更新非 null 且非空字符串的字段。
     *
     * <p>空字符串被视为“不修改”，因此无法通过该方法清空已有资料。</p>
     *
     * @param userId 用户 ID
     * @param nickname 新昵称，可为 null 或空串表示不修改
     * @param phone 新手机号，可为 null 或空串表示不修改
     * @param email 新邮箱，可为 null 或空串表示不修改
     * @param avatar 新头像 URL，可为 null 或空串表示不修改
     * @return 受影响行数，0 表示用户不存在
     */
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

    /**
     * 更新用户角色。
     *
     * @param id 用户 ID
     * @param role 目标角色：1-普通用户，2-商家，3-管理员
     * @return 受影响行数，0 表示用户不存在
     */
    @Update("update user set role = #{role}, updated_at = now() where id = #{id}")
    int updateRole(@Param("id") Long id, @Param("role") Integer role);

    /**
     * 管理后台分页查询用户。
     *
     * @param keyword 关键字，模糊匹配用户名或手机号；为空时不过滤
     * @param role 角色筛选：1-普通用户，2-商家，3-管理员；为 null 时不过滤
     * @param status 账号状态筛选：0-正常，1-禁用；为 null 时不过滤
     * @param offset 偏移量，从 0 开始
     * @param size 每页条数
     * @return 用户列表，按 ID 倒序
     */
    @Select("""
            <script>
            SELECT id, username, nickname, phone, email, role, status, avatar, created_at, updated_at
            FROM user WHERE 1=1
            <if test="keyword != null and keyword != ''">
                AND (username LIKE CONCAT('%', #{keyword}, '%') OR phone LIKE CONCAT('%', #{keyword}, '%'))
            </if>
            <if test="role != null"> AND role = #{role} </if>
            <if test="status != null"> AND status = #{status} </if>
            ORDER BY id DESC LIMIT #{offset}, #{size}
            </script>
            """)
    List<User> selectAdminPage(@Param("keyword") String keyword, @Param("role") Integer role,
                               @Param("status") Integer status, @Param("offset") int offset, @Param("size") int size);

    /**
     * 统计管理后台用户数量，用于分页。
     *
     * @param keyword 关键字，模糊匹配用户名或手机号；为空时不过滤
     * @param role 角色筛选，为 null 时不过滤
     * @param status 账号状态筛选，为 null 时不过滤
     * @return 用户总数
     */
    @Select("""
            <script>
            SELECT COUNT(*) FROM user WHERE 1=1
            <if test="keyword != null and keyword != ''">
                AND (username LIKE CONCAT('%', #{keyword}, '%') OR phone LIKE CONCAT('%', #{keyword}, '%'))
            </if>
            <if test="role != null"> AND role = #{role} </if>
            <if test="status != null"> AND status = #{status} </if>
            </script>
            """)
    long countAdmin(@Param("keyword") String keyword, @Param("role") Integer role, @Param("status") Integer status);

    /**
     * 更新用户账号状态。
     *
     * <p>禁用后需由认证流程清除 Redis 中的登录态，使既有 token 立即失效。</p>
     *
     * @param id 用户 ID
     * @param status 目标状态：0-正常，1-禁用
     * @return 受影响行数，0 表示用户不存在
     */
    @Update("update user set status = #{status}, updated_at = now() where id = #{id}")
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);
}
