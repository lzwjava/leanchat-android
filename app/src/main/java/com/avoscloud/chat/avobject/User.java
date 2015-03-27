package com.avoscloud.chat.avobject;

import com.avos.avoscloud.AVFile;
import com.avos.avoscloud.AVInstallation;
import com.avos.avoscloud.AVUser;
import com.avoscloud.chat.R;
import com.avoscloud.chat.base.App;

public class User {
  public static final String USERNAME = "username";
  public static final String PASSWORD = "password";
  public static final String AVATAR = "avatar";
  public static final String FRIENDS = "friends";
  public static final String LOCATION = "location";
  public static final String GENDER = "gender";
  public static final String INSTALLATION = "installation";

  public static String[] genderStrings = new String[]{App.ctx.getString(R.string.male),
      App.ctx.getString(R.string.female)};

  public static String getCurrentUserId() {
    AVUser user = AVUser.getCurrentUser();
    if (user != null) {
      return user.getObjectId();
    } else {
      return null;
    }
  }

  public static String getAvatarUrl(AVUser user) {
    AVFile avatar = user.getAVFile(AVATAR);
    if (avatar != null) {
      return avatar.getUrl();
    } else {
      return null;
    }
  }

  public static Gender getGender(AVUser user) {
    int genderInt = user.getInt(GENDER);
    return Gender.fromInt(genderInt);
  }

  public static void setGender(AVUser user, Gender gender) {
    user.put(GENDER, gender.getValue());
  }

  public static String getGenderDesc(AVUser user) {
    Gender gender = getGender(user);
    return genderStrings[gender.getValue()];
  }

  public static AVInstallation getInstallation(AVUser user) {
    try {
      return user.getAVObject(INSTALLATION, AVInstallation.class);
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }

  public static enum Gender {
    Male(0), Female(1);

    int value;

    Gender(int value) {
      this.value = value;
    }

    public static Gender fromInt(int index) {
      return values()[index];
    }

    public int getValue() {
      return value;
    }
  }
}
