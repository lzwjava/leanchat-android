package com.lzw.talk.avobject;

import com.avos.avoscloud.*;

import java.io.IOException;

/**
 * Created by lzw on 14-6-26.
 */
@AVClassName("User")
public class User extends AVUser {
  public static final String USERNAME = "username";
  public static final String PASSWORD = "password";
  public static final String AVATAR = "avatar";
  public static final String NICKNAME = "nickname";
  //AVFile avatar;
  //String nickname;

  public User() {
  }

  public static User curUser() {
    AVUser user = User.getCurrentUser(User.class);
    return User.cast(user, User.class);
  }

  public AVFile getAvatar() {
    return getAVFile(AVATAR);
  }

  public void setAvatar(AVFile avatar) {
    put(AVATAR, avatar);
  }

  public String getAvatarUrl() {
    AVFile avatar = getAvatar();
    if (avatar != null) {
      return avatar.getUrl();
    } else {
      return null;
    }
  }

  public String getNickname() {
    return getString(NICKNAME);
  }

  public void setNickname(String nickname) {
    put(NICKNAME, nickname);
  }

  public void saveAvatar(String path) {
    try {
      final AVFile file = AVFile.withAbsoluteLocalPath(getUsername(), path);
      file.saveInBackground(new SaveCallback() {
        @Override
        public void done(AVException e) {
          setAvatar(file);
          saveInBackground();
        }
      });
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
