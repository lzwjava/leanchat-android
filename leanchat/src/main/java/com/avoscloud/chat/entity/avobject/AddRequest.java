package com.avoscloud.chat.entity.avobject;

import com.avos.avoscloud.AVClassName;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVUser;

/**
 * Created by lzw on 14-9-27.
 */
@AVClassName("AddRequest")
public class AddRequest extends AVObject {
  public static final int STATUS_WAIT = 0;
  public static final int STATUS_DONE = 1;

  public static final String FROM_USER = "fromUser";
  public static final String TO_USER = "toUser";
  public static final String STATUS = "status";

  /**
   * 标记接收方是否已读该消息
   */
  public static final String IS_READ = "isRead";

  AVUser toUser;
  //int status;
  AVUser fromUser;

  public AddRequest() {
  }

  public synchronized AVUser getFromUser() {
    if (fromUser == null) {
      fromUser = getAVUser(FROM_USER, AVUser.class);
    }
    return fromUser;
  }

  public void setFromUser(AVUser fromUser) {
    put(FROM_USER, fromUser);
  }

  public synchronized AVUser getToUser() {
    if (toUser == null) {
      toUser = getAVUser(TO_USER, AVUser.class);
    }
    return toUser;
  }

  public void setToUser(AVUser toUser) {
    put(TO_USER, toUser);
  }

  public int getStatus() {
    return getInt(STATUS);
  }

  public void setStatus(int status) {
    put(STATUS, status);
  }

  public boolean isRead() {
    return getBoolean(IS_READ);
  }

  public void setIsRead(boolean isRead) {
    put(IS_READ, isRead);
  }
}
