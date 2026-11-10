package com.ncist.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 会话联查结果（含商家店铺名/头像、关联商品名）
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ConversationDetail extends Conversation {

    private String merchantName;
    private String merchantAvatar;
    private String productName;
}
