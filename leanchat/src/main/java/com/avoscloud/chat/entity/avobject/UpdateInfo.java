package com.avoscloud.chat.entity.avobject;

import com.avos.avoscloud.AVClassName;
import com.avos.avoscloud.AVObject;

@AVClassName("UpdateInfo")
public class UpdateInfo extends AVObject {
  public static final String DESC = "desc";
  public static String VERSION = "version";
  public static String APK_URL = "apkUrl";

  public UpdateInfo() {
  }

  public int getVersion() {
    return getInt(VERSION);
  }

  public void setVersion(int version) {
    put(VERSION, version);
  }

  public String getApkUrl() {
    return getString(APK_URL);
  }

  public void setApkUrl(String apkUrl) {
    put(APK_URL, apkUrl);
  }

  public String getDesc() {
    return getString(DESC);
  }

  public void setDesc(String desc) {
    put(DESC, desc);
  }
}
