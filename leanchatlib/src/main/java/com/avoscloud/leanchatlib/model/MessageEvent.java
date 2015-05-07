package com.avoscloud.leanchatlib.model;

import com.avos.avoscloud.im.v2.AVIMTypedMessage;

/**
 * Created by lzw on 15/3/4.
 */
public class MessageEvent {
  public enum Type {
    Come, Receipt
  }

  private AVIMTypedMessage msg;
  private Type type;

  public MessageEvent(AVIMTypedMessage msg, Type type) {
    this.msg = msg;
    this.type = type;
  }

  public AVIMTypedMessage getMsg() {
    return msg;
  }

  public Type getType() {
    return type;
  }
}
