package com.avoscloud.chat.avobject;

import com.avos.avoscloud.AVClassName;
import com.avos.avoscloud.AVFile;
import com.avos.avoscloud.AVGeoPoint;
import com.avos.avoscloud.AVUser;
import com.avoscloud.chat.R;
import com.avoscloud.chat.base.App;

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
  public static final String SEX = "sex";
  //AVFile avatar;
  //AVGeoPoint location;
  private String sortLetters;
  //private boolean sex;// true is male ; false is female
  private static User curUser;

  public User() {
  }

  public static User curUser() {
    if (curUser == null) {
      curUser = getCurrentUser(User.class);
    }
    return curUser;
  }

  public static void setCurUser(User curUser) {
    User.curUser = curUser;
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
    return getBoolean(SEX);
  }

  public void setSex(boolean isMale) {
    put(SEX, isMale);
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
