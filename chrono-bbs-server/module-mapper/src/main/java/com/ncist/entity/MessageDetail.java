package com.ncist.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 消息联查结果（含发送者昵称）
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class MessageDetail extends Message {

    private String senderName;
}
