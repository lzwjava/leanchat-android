package com.avoscloud.chat.entity;

import com.avos.avoscloud.im.v2.AVIMConversation;
import com.avos.avoscloud.im.v2.AVIMTypedMessage;

/**
 * Created by lzw on 14-9-26.
 */
public class Room {
  private AVIMTypedMessage lastMsg;
  private AVIMConversation conv;
  private String convid;
  private int unreadCount;

  public AVIMTypedMessage getLastMsg() {
    return lastMsg;
  }

  public void setLastMsg(AVIMTypedMessage lastMsg) {
    this.lastMsg = lastMsg;
  }

  public AVIMConversation getConv() {
    return conv;
  }

  public void setConv(AVIMConversation conv) {
    this.conv = conv;
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
