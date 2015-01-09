package com.avoscloud.chat.entity;

import com.avos.avoscloud.AVUser;
import com.avoscloud.chat.avobject.ChatGroup;

/**
 * Created by lzw on 14-9-26.
 */
public class Conversation {
  private Msg msg;
  private AVUser toUser;
  private ChatGroup toChatGroup;
  private int unreadCount;

  public Msg getMsg() {
    return msg;
  }

  public void setMsg(Msg msg) {
    this.msg = msg;
  }

  public AVUser getToUser() {
    return toUser;
  }

  public void setToUser(AVUser toUser) {
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
