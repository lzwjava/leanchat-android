package com.avoscloud.leanchatlib.model;

import com.avos.avoscloud.im.v2.AVIMTypedMessage;

/**
 * Created by lzw on 15/3/4.
 */
public class MessageEvent {
  private AVIMTypedMessage msg;

  public MessageEvent(AVIMTypedMessage msg) {
    this.msg = msg;
  }

  public AVIMTypedMessage getMsg() {
    return msg;
  }
}
