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

  public static String checkAndMkdirs(String dir) {
    File file = new File(dir);
    file.mkdirs();
    return dir;
  }

  public static String getAppPath() {
    String dir = getSDcardDir() + "leanchat/";
    return checkAndMkdirs(dir);
  }

  public static String getAvatarDir() {
    String dir = getAppPath() + "avatar/";
    return checkAndMkdirs(dir);
  }

  public static String getAvatarTmpPath() {
    return getAvatarDir() + "tmp";
  }

  public static String getChatFileDir() {
    String dir = getAppPath() + "chat_file/";
    return checkAndMkdirs(dir);
  }

  public static String getRecordTmpPath() {
    return getChatFileDir() + "tmp";
  }

  public static String getUUIDFilePath() {
    return getChatFileDir() + Utils.uuid();
  }

  public static String getTmpPath() {
    return getAppPath() + "tmp";
  }
}
