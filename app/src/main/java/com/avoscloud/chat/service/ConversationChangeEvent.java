package com.avoscloud.chat.service;

import com.avos.avoscloud.im.v2.AVIMConversation;

/**
 * Created by lzw on 15/3/5.
 */
//name, member change event
public class ConversationChangeEvent {
  private AVIMConversation conv;

  public ConversationChangeEvent(AVIMConversation conv) {
    this.conv = conv;
  }

  public AVIMConversation getConv() {
    return conv;
  }
}
