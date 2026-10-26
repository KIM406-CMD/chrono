package com.ncist.common.context;

public class UserContext {

    private static final ThreadLocal<UserInfo> USER_HOLDER = new ThreadLocal<>();

    private UserContext() {
    }

    public static void set(UserInfo userInfo) {
        USER_HOLDER.set(userInfo);
    }

    public static UserInfo get() {
        return USER_HOLDER.get();
    }

    public static Long getUserId() {
        UserInfo userInfo = get();
        return userInfo != null ? userInfo.getUserId() : null;
    }

    public static String getUsername() {
        UserInfo userInfo = get();
        return userInfo != null ? userInfo.getUsername() : null;
    }

    public static Integer getRole() {
        UserInfo userInfo = get();
        return userInfo != null ? userInfo.getRole() : null;
    }

    public static void clear() {
        USER_HOLDER.remove();
    }

    public static class UserInfo {
        private Long userId;
        private String username;
        private Integer role;

        public UserInfo() {
        }

        public UserInfo(Long userId, String username, Integer role) {
            this.userId = userId;
            this.username = username;
            this.role = role;
        }

        public Long getUserId() {
            return userId;
        }

        public void setUserId(Long userId) {
            this.userId = userId;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public Integer getRole() {
            return role;
        }

        public void setRole(Integer role) {
            this.role = role;
        }
    }
}