package com.avoscloud.leanchatlib.utils;

import com.avoscloud.leanchatlib.controller.ChatManager;

import java.io.File;

/**
 * Created by lzw on 15/4/26.
 */
public class PathUtils {
  public static String checkAndMkdirs(String dir) {
    File file = new File(dir);
    if (!file.exists()) {
      file.mkdirs();
    }
    return dir;
  }

  public static String getCacheDir() {
    return ChatManager.getContext().getCacheDir().getAbsolutePath() + "/";
  }

  public static String getChatFileDir() {
    String dir = getCacheDir() + "files/";
    return checkAndMkdirs(dir);
  }

  public static String getChatFilePath(String id) {
    String dir = getChatFileDir();
    String path = dir + id;
    return path;
  }

  public static String getRecordTmpPath() {
    return getChatFileDir() + "record_tmp";
  }

  public static String getTmpPath() {
    return getCacheDir() + "com.avoscloud.chat.tmp";
  }
}
