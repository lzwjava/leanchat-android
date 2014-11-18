package com.avoscloud.chat.avobject;

import com.avos.avoscloud.AVClassName;
import com.avos.avoscloud.AVObject;

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
  //User fromUser;
  //User toUser;
  //int status;

  public AddRequest() {
  }

  public User getFromUser() {
    return getAVUser(FROM_USER, User.class);
  }

  public void setFromUser(User fromUser) {
    put(FROM_USER, fromUser);
  }

  public User getToUser() {
    return getAVUser(TO_USER, User.class);
  }

  public void setToUser(User toUser) {
    put(TO_USER, toUser);
  }

  public int getStatus() {
    return getInt(STATUS);
  }

  public void setStatus(int status) {
    put(STATUS, status);
  }
}
