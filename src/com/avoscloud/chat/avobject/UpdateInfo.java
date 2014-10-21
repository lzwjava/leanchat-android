package com.avoscloud.chat.avobject;

import com.avos.avoscloud.AVClassName;
import com.avos.avoscloud.AVObject;

@AVClassName("UpdateInfo")
public class UpdateInfo extends AVObject {
  public static final String DESC = "desc";
  public static final String APP_NAME = "appName";
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

  public String getAppName() {
    return getString(APP_NAME);
  }

  public void setAppName(String appName) {
    put(APP_NAME, appName);
  }
}
