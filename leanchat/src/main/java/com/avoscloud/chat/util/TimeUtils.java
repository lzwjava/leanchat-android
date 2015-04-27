package com.avoscloud.chat.util;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by lzw on 14-6-28.
 */
public class TimeUtils {

  public static String getDate(Date date) {
    SimpleDateFormat format = new SimpleDateFormat("MM-dd HH:mm");
    return format.format(date);
  }

}
