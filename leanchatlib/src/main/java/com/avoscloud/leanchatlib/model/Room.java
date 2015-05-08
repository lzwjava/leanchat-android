package com.avoscloud.leanchatlib.model;

import com.avos.avoscloud.im.v2.AVIMConversation;
import com.avos.avoscloud.im.v2.AVIMTypedMessage;

/**
 * Created by lzw on 14-9-26.
 */
public class Room {
  private AVIMTypedMessage lastMessage;
  private AVIMConversation conversation;
  private String convid;
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

  public String getConvid() {
    return convid;
  }

  public void setConvid(String convid) {
    this.convid = convid;
  }

  public int getUnreadCount() {
    return unreadCount;
  }

  public void setUnreadCount(int unreadCount) {
    this.unreadCount = unreadCount;
  }
}
