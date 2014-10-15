package com.avoscloud.chat.util;

import android.view.MenuItem;

/**
 * Created by lzw on 14-10-12.
 */
public class UIUtils {
  public static void alwaysShowMenuItem(MenuItem add) {
    add.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS
        | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
  }
}
