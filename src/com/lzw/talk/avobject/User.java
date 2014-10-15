package com.lzw.talk.avobject;

import com.avos.avoscloud.*;
import com.baidu.platform.comapi.map.g;
import com.lzw.talk.R;
import com.lzw.talk.base.App;

import java.io.IOException;

/**
 * Created by lzw on 14-6-26.
 */
@AVClassName("_User")
public class User extends AVUser {
  public static final String USERNAME = "username";
  public static final String PASSWORD = "password";
  public static final String AVATAR = "avatar";
  public static final String FRIENDS = "friends";
  public static final String LOCATION = "location";
  //AVFile avatar;
  //AVGeoPoint location;
  private String sortLetters;
  private boolean sex;// true is male ; false is female

  public User() {
  }

  public static User curUser() {
    AVUser avUser = getCurrentUser(User.class);
    User user = User.cast(avUser, User.class);
    return user;
  }

  public static String curUserId() {
    User user = curUser();
    if (user != null) {
      return user.getObjectId();
    } else {
      return null;
    }
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

  public void saveAvatar(String path) {
    try {
      final AVFile file = AVFile.withAbsoluteLocalPath(getUsername(), path);
      file.saveInBackground(new SaveCallback() {
        @Override
        public void done(AVException e) {
          if (e== null) {
            setAvatar(file);
            saveInBackground();
          } else {
            e.printStackTrace();
          }
        }
      });
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void addFriend(User user) {
    getRelation(FRIENDS).add(user);
  }


  public void removeFriend(User user) {
    getRelation(FRIENDS).remove(user);
  }

  public AVGeoPoint getLocation() {
    return getAVGeoPoint(LOCATION);
  }

  public void setLocation(AVGeoPoint location) {
    put(LOCATION, location);
  }

  public boolean getSex() {
    return sex;
  }

  public void setSex(boolean isMale) {
    this.sex = isMale;
  }

  public String getSexInfo() {
    return getSex() ? App.ctx.getString(R.string.male) :
        App.ctx.getString(R.string.female);
  }

  public String getSortLetters() {
    return sortLetters;
  }

  public void setSortLetters(String sortLetters) {
    this.sortLetters = sortLetters;
  }
}
