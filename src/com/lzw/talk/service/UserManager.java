package com.lzw.talk.service;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.AVUser;
import com.lzw.talk.avobject.User;

import java.util.List;

/**
 * Created by lzw on 14-9-15.
 */
public class UserManager {
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
    q.whereEqualTo(User.USERNAME, username);
    q.setLimit(1);
    return q;
  }
}
