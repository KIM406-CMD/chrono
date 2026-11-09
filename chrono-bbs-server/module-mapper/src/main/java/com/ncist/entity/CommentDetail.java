package com.ncist.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 评论联查结果（含用户昵称/头像）。
 *
 * <p>非数据库实体，用于评论列表展示：在 {@link Comment} 基础上附带评论人信息。</p>
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class CommentDetail extends Comment {

    /** 评论人登录用户名。 */
    private String username;
    /** 评论人昵称，优先用于展示。 */
    private String nickname;
    /** 评论人头像 URL。 */
    private String avatar;
}
