package com.avoscloud.leanchatlib.model;

import com.avos.avoscloud.im.v2.AVIMConversation;
import com.avos.avoscloud.im.v2.AVIMTypedMessage;

/**
 * Created by lzw on 14-9-26.
 */
public class Room {
  private AVIMTypedMessage lastMessage;
  private AVIMConversation conversation;
  private String conversationId;
  private int unreadCount;

  public AVIMTypedMessage getLastMessage() {
    return lastMessage;
  }

  public void setLastMessage(AVIMTypedMessage lastMessage) {
    this.lastMessage = lastMessage;
  }

  public AVIMConversation getConversation() {
    return conversation;
  }

  public void setConversation(AVIMConversation conversation) {
    this.conversation = conversation;
  }

  public String getConversationId() {
    return conversationId;
  }

  public void setConversationId(String conversationId) {
    this.conversationId = conversationId;
  }

  public int getUnreadCount() {
    return unreadCount;
  }

  public void setUnreadCount(int unreadCount) {
    this.unreadCount = unreadCount;
  }
}
