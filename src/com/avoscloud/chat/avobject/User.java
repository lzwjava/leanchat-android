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
  public static final String GENDER = "gender";

  public static String[] genderStrings = new String[]{App.ctx.getString(R.string.male),
      App.ctx.getString(R.string.female)};

  //AVFile avatar;
  //AVGeoPoint location;
  private String sortLetters;
  //Gender gender;
  private static User curUser;

  public static enum Gender {
    Male(0), Female(1);

    int value;

    Gender(int value) {
      this.value = value;
    }

    public int getValue() {
      return value;
    }

    public static Gender fromInt(int index) {
      return values()[index];
    }
  }

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

  public Gender getGender() {
    int genderInt = getInt(GENDER);
    return Gender.fromInt(genderInt);
  }

  public void setGender(Gender gender) {
    put(GENDER, gender.getValue());
  }

  public String getGenderDesc() {
    Gender gender = getGender();
    return genderStrings[gender.getValue()];
  }

  public String getSortLetters() {
    return sortLetters;
  }

  public void setSortLetters(String sortLetters) {
    this.sortLetters = sortLetters;
  }
}
