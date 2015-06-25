package com.avoscloud.leanchatlib.utils;

import android.os.Environment;

import java.io.File;

/**
 * Created by lzw on 15/4/26.
 */
public class PathUtils {
  private static File checkAndMkdirs(File file) {
    if (!file.exists()) {
      file.mkdirs();
    }
    return file;
  }

  private static File getCacheDir() {
    File sdcard = Environment.getExternalStorageDirectory();
    File leanchatDir = new File(sdcard, "leanchat");
    return leanchatDir;
  }

  private static File getChatFileDir() {
    File filesDir = new File(getCacheDir(), "files");
    return checkAndMkdirs(filesDir);
  }

  public static String getChatFilePath(String id) {
    return new File(getChatFileDir(), id).getAbsolutePath();
  }

  public static String getRecordTmpPath() {
    return new File(getChatFileDir(), "record_tmp").getAbsolutePath();
  }

  public static String getPicturePath() {
    return new File(getChatFileDir(), "picture_tmp").getAbsolutePath();
  }
}
