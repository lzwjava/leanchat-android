package com.avoscloud.chat.service.event;

import com.avos.avoscloud.im.v2.AVIMTypedMessage;

/**
 * Created by lzw on 15/3/4.
 */
public class MsgEvent {
  private AVIMTypedMessage msg;

  public MsgEvent(AVIMTypedMessage msg) {
    this.msg = msg;
  }

  public AVIMTypedMessage getMsg() {
    return msg;
  }
}
