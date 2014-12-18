package com.avoscloud.chat.entity;

import com.avoscloud.chat.avobject.ChatGroup;
import com.avoscloud.chat.avobject.User;

/**
 * Created by lzw on 14-9-26.
 */
public class Conversation {
  private Msg msg;
  private User toUser;
  private ChatGroup toChatGroup;
  private int unreadCount;

  public Msg getMsg() {
    return msg;
  }

  public void setMsg(Msg msg) {
    this.msg = msg;
  }

  public User getToUser() {
    return toUser;
  }

  public void setToUser(User toUser) {
    this.toUser = toUser;
  }

  public ChatGroup getToChatGroup() {
    return toChatGroup;
  }

  public void setToChatGroup(ChatGroup toChatGroup) {
    this.toChatGroup = toChatGroup;
  }

  public int getUnreadCount() {
    return unreadCount;
  }

  public void setUnreadCount(int unreadCount) {
    this.unreadCount = unreadCount;
  }
}
