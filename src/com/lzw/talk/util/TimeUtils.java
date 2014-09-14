package com.lzw.talk.util;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by lzw on 14-6-28.
 */
public class TimeUtils {
  public static String getDate(Date date) {
    Calendar c = Calendar.getInstance();
    c.setTime(date);

    String month = String.valueOf(c.get(Calendar.MONTH));
    String day = String.valueOf(c.get(Calendar.DAY_OF_MONTH) + 1);
    String hour = String.valueOf(c.get(Calendar.HOUR_OF_DAY));
    String mins = String.valueOf(c.get(Calendar.MINUTE));
    String secs=String.valueOf(c.get(Calendar.SECOND));

    StringBuffer sbBuffer = new StringBuffer();
    sbBuffer.append(month + "-" + day + " " + hour + ":" + mins+":"+secs);
    return sbBuffer.toString();
  }
}
