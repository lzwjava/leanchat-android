package com.avoscloud.chat.service.event;

import com.avos.avoscloud.im.v2.AVIMConversation;

/**
 * Created by lzw on 15/3/5.
 */
//name, member change event
public class ConvChangeEvent {
  private AVIMConversation conv;

  public ConvChangeEvent(AVIMConversation conv) {
    this.conv = conv;
  }

  public AVIMConversation getConv() {
    return conv;
  }
}
