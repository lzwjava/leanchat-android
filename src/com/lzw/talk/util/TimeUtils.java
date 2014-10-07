package com.lzw.talk.util;

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

  public static String millisecs2DateString(long timestamp) {
    return getDate(new Date(timestamp));
  }

  public static boolean haveTimeGap(long lastTime, long time) {
    int gap = 1000 * 60 * 10;
    return time - lastTime > gap;
  }
}
