package com.ncist.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 评论联查结果（含用户昵称/头像）
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class CommentDetail extends Comment {

    private String username;
    private String nickname;
    private String avatar;
}
