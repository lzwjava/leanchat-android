package com.lzw.talk.avobject;

import com.avos.avoscloud.*;
import com.lzw.talk.R;
import com.lzw.talk.base.App;

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
  public static final String FRIENDS = "friends";
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
    String name = getString(NICKNAME);
    if (name == null) {
      return App.ctx.getString(R.string.anonymous);
    }
    return name;
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

  public void addFriend(User user) {
    AVRelation<User> friendsRelation = getRelation("friends");
    friendsRelation.add(user);
  }

  public void removeFriend(User user) {
    getRelation(FRIENDS).remove(user);
  }
}
