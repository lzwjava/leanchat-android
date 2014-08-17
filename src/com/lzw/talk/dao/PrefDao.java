package com.lzw.talk.dao;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by lzw on 14-6-19.
 */
public class PrefDao {
  Context cxt;
  SharedPreferences pref;
  SharedPreferences.Editor editor;

  public PrefDao(Context cxt) {
    this.cxt = cxt;
    pref = PreferenceManager.getDefaultSharedPreferences(cxt);
    editor = pref.edit();
  }
}
