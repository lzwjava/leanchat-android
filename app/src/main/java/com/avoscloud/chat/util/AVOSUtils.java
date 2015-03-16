package com.avoscloud.chat.util;

import com.avos.avoscloud.AVObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lzw on 15/2/13.
 */
public class AVOSUtils {
  public static List<String> getObjectIds(List<? extends AVObject> objects) {
    List<String> ids = new ArrayList<String>();
    for (AVObject o : objects) {
      ids.add(o.getObjectId());
    }
    return ids;
  }
}
