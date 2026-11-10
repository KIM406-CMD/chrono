package com.ncist.common.context;

/**
 * 当前请求的用户上下文。
 *
 * <p>基于 {@link ThreadLocal} 在单次请求链路中传递登录用户信息，
 * 由认证拦截器在请求开始时写入、请求结束时清理，避免在业务层重复解析登录态。</p>
 */
public class UserContext {

    /** 保存当前请求登录用户信息的线程局部变量。 */
    private static final ThreadLocal<UserInfo> USER_HOLDER = new ThreadLocal<>();

    private UserContext() {
    }

    /**
     * 写入当前请求的登录用户信息。
     *
     * @param userInfo 登录用户信息；传 {@code null} 等同于清理
     */
    public static void set(UserInfo userInfo) {
        USER_HOLDER.set(userInfo);
    }

    /**
     * 获取当前请求的登录用户信息。
     *
     * @return 登录用户信息；未登录时返回 {@code null}
     */
    public static UserInfo get() {
        return USER_HOLDER.get();
    }

    /**
     * 获取当前登录用户 ID。
     *
     * @return 用户 ID；未登录时返回 {@code null}
     */
    public static Long getUserId() {
        UserInfo userInfo = get();
        return userInfo != null ? userInfo.getUserId() : null;
    }

    /**
     * 获取当前登录用户名。
     *
     * @return 用户名；未登录时返回 {@code null}
     */
    public static String getUsername() {
        UserInfo userInfo = get();
        return userInfo != null ? userInfo.getUsername() : null;
    }

    /**
     * 获取当前登录用户角色。
     *
     * @return 角色编码；未登录时返回 {@code null}
     * @see com.ncist.common.enums.UserRoleEnum
     */
    public static Integer getRole() {
        UserInfo userInfo = get();
        return userInfo != null ? userInfo.getRole() : null;
    }

    /**
     * 清理当前请求的用户上下文。
     *
     * <p>必须在请求结束时调用，避免线程复用导致用户信息串用。</p>
     */
    public static void clear() {
        USER_HOLDER.remove();
    }

    /**
     * 请求级登录用户信息。
     */
    public static class UserInfo {
        /** 用户 ID。 */
        private Long userId;
        /** 登录用户名。 */
        private String username;
        /** 角色编码，取值见 {@link com.ncist.common.enums.UserRoleEnum}。 */
        private Integer role;

        /**
         * 创建空的用户信息对象。
         */
        public UserInfo() {
        }

        /**
         * 创建登录用户信息。
         *
         * @param userId   用户 ID
         * @param username 登录用户名
         * @param role     角色编码
         */
        public UserInfo(Long userId, String username, Integer role) {
            this.userId = userId;
            this.username = username;
            this.role = role;
        }

        /**
         * 获取用户 ID。
         *
         * @return 用户 ID
         */
        public Long getUserId() {
            return userId;
        }

        /**
         * 设置用户 ID。
         *
         * @param userId 用户 ID
         */
        public void setUserId(Long userId) {
            this.userId = userId;
        }

        /**
         * 获取登录用户名。
         *
         * @return 登录用户名
         */
        public String getUsername() {
            return username;
        }

        /**
         * 设置登录用户名。
         *
         * @param username 登录用户名
         */
        public void setUsername(String username) {
            this.username = username;
        }

        /**
         * 获取角色编码。
         *
         * @return 角色编码
         */
        public Integer getRole() {
            return role;
        }

        /**
         * 设置角色编码。
         *
         * @param role 角色编码
         */
        public void setRole(Integer role) {
            this.role = role;
        }
    }
}