package com.avoscloud.leanchatlib.model;

import com.avos.avoscloud.im.v2.AVIMTypedMessage;

/**
 * Created by lzw on 15/3/4.
 */
public class MessageEvent {
  public enum Type {
    Come, Receipt
  }

  private AVIMTypedMessage message;
  private Type type;

  public MessageEvent(AVIMTypedMessage message, Type type) {
    this.message = message;
    this.type = type;
  }

  public AVIMTypedMessage getMessage() {
    return message;
  }

  public Type getType() {
    return type;
  }
}
