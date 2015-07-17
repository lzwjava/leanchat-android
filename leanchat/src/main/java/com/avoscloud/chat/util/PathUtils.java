package com.avoscloud.chat.util;

import android.os.Environment;
import com.avoscloud.chat.base.App;

import java.io.File;

/**
 * Created by lzw on 14-9-19.
 */
public class PathUtils {

  private static boolean isExternalStorageWritable() {
    String state = Environment.getExternalStorageState();
    return Environment.MEDIA_MOUNTED.equals(state);
  }

  private static File getAvailableCacheDir() {
    if (isExternalStorageWritable()) {
      return App.ctx.getExternalCacheDir();
    } else {
      return App.ctx.getCacheDir();
    }
  }

  public static String checkAndMkdirs(String dir) {
    File file = new File(dir);
    if (file.exists() == false) {
      file.mkdirs();
    }
    return dir;
  }

  public static String getAvatarCropPath() {
    return new File(getAvailableCacheDir(), "avatar_crop").getAbsolutePath();
  }

  public static String getAvatarTmpPath() {
    return new File(getAvailableCacheDir(), "avatar_tmp").getAbsolutePath();
  }

}
