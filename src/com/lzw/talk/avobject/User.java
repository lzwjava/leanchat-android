package com.lzw.talk.avobject;

import com.avos.avoscloud.*;

import java.util.List;

/**
 * Created by lzw on 14-6-26.
 */
@AVClassName("User")
public class User extends AVUser{
  public static final String USERNAME = "username";
  public static final String PASSWORD = "password";
  public User(){
  }

  public static User curUser() {
    AVUser user=User.getCurrentUser(User.class);
    return User.cast(user,User.class);
  }
}
