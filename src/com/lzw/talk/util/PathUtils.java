package com.lzw.talk.util;

import android.os.Environment;

import java.io.File;

/**
 * Created by lzw on 14-9-19.
 */
public class PathUtils {
  public static String getSDcardDir() {
    return Environment.getExternalStorageDirectory().getPath() + "/";
  }

  public static void checkAndMkdirs(String dir) {
    File file = new File(dir);
    if (file.exists() == false) file.mkdirs();
  }

  public static String getAppPath() {
    String dir = getSDcardDir() + "leanchat/";
    checkAndMkdirs(dir);
    return dir;
  }

  public static String getAvatarDir() {
    String appPath = getAppPath();
    String dir = appPath + "avatar/";
    checkAndMkdirs(dir);
    return dir;
  }

  public static String getImageDir(){
    String dir=getAppPath()+"image/";
    checkAndMkdirs(dir);
    return dir;
  }
}
