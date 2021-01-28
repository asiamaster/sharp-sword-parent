package com.dili.ss.redis.delayqueue.dto;

import com.dili.ss.dto.IDTO;

import java.time.LocalDateTime;

/**
 *  消息体
 *
 *  @author asiamaster
 *  @date 2021-01-26
 */
public interface DelayMessage extends IDTO {
  /**
   *  消息唯一标识
   */

  String getId();
  void setId(String id);

  /**
   *  消息主题
   */

  String getTopic();
  void setTopic(String topic);

  /**
   *  具体消息 json
   */
  String getBody();
  void setBody(String body);

  /**
   *  延时时间, 格式为时间戳: 当前时间戳 + 实际延迟毫秒数
   */
  Long getDelayTime();
  void setDelayTime(Long delayTime);

  /**
   *  延时时长，单位秒
   *  当前时间往后延时秒数
   */
  Long getDelayDuration();
  void setDelayDuration(Long delayDuration);

  /**
   *  消息发送时间
   */
  LocalDateTime getCreateTime();
  void setCreateTime(LocalDateTime createTime);
}
