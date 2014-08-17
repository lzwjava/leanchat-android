package com.lzw.talk.avobject;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.AVUser;

import java.util.List;

/**
 * Created by lzw on 14-6-26.
 */
public class User {
  public static final String USERNAME = "username";
  public static final String PASSWORD = "password";

  public static AVUser curUser() {
    return AVUser.getCurrentUser();
  }

  public static AVUser getAVUser(String username) throws AVException {
    AVQuery<AVUser> q = getUserQuery(username);
    List<AVUser> users = q.find();
    if (users != null && users.isEmpty() == false) {
      return users.get(0);
    }
    return null;
  }

  public static AVQuery<AVUser> getUserQuery(String username) {
    AVQuery<AVUser> q = AVObject.getQuery(AVUser.class);
    q.whereEqualTo(USERNAME, username);
    q.setLimit(1);
    return q;
  }

  public static boolean isEqualCurrentUser(AVUser fromUser) {
    AVUser curUser = AVUser.getCurrentUser();
    return fromUser.getUsername().equals(curUser.getUsername());
  }

  public static boolean isEqual(AVUser fromUser, AVUser currentUser) {
    return fromUser.getUsername().equals(currentUser.getUsername());
  }

  public static String getMyName() {
    return AVUser.getCurrentUser().getUsername();
  }
}
