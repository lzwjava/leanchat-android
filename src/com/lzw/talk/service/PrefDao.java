package com.lzw.talk.service;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import com.avos.avoscloud.AVGeoPoint;
import com.lzw.talk.avobject.User;
import com.lzw.talk.util.Logger;

/**
 * Created by lzw on 14-6-19.
 */
public class PrefDao {
  public static final String ADD_REQUEST_N = "addRequestN";
  public static final String LATITUDE = "latitude";
  public static final String LONGITUDE = "longitude";
  Context cxt;
  SharedPreferences pref;
  SharedPreferences.Editor editor;
  //int addRequestN;
  //String latitude;
  //String longitude;

  public PrefDao(Context cxt) {
    this.cxt = cxt;
    pref = PreferenceManager.getDefaultSharedPreferences(cxt);
    editor = pref.edit();
    Logger.d("PrefDao init no specific user");
  }

  public PrefDao(Context cxt, String prefName) {
    this.cxt = cxt;
    pref = cxt.getSharedPreferences(prefName, Context.MODE_PRIVATE);
    editor = pref.edit();
  }

  public static PrefDao getCurUserPrefDao(Context ctx) {
    User user = User.curUser();
    if (user == null) {
      throw new NullPointerException("user is null");
    }
    PrefDao prefDao = new PrefDao(ctx, user.getObjectId());
    return prefDao;
  }

  public static PrefDao getMyPrefDao(Context ctx) {
    User user = User.curUser();
    if (user == null) {
      throw new RuntimeException("user is null");
    }
    return new PrefDao(ctx, user.getObjectId());
  }

  public int getAddRequestN() {
    return pref.getInt(ADD_REQUEST_N, 0);
  }

  public void setAddRequestN(int addRequestN) {
    editor.putInt(ADD_REQUEST_N, addRequestN).commit();
  }

  private String getLatitude() {
    return pref.getString(LATITUDE, null);
  }

  private void setLatitude(String latitude) {
    editor.putString(LATITUDE, latitude).commit();
  }

  private String getLongitude() {
    return pref.getString(LONGITUDE, null);
  }

  private void setLongitude(String longitude) {
    editor.putString(LONGITUDE, longitude).commit();
  }

  public AVGeoPoint getLocation() {
    String latitudeStr = getLatitude();
    String longitudeStr = getLongitude();
    if (latitudeStr == null || longitudeStr == null) {
      return null;
    }
    double latitude = Double.parseDouble(latitudeStr);
    double longitude = Double.parseDouble(longitudeStr);
    return new AVGeoPoint(latitude, longitude);
  }

  public void setLocation(AVGeoPoint location) {
    if (location == null) {
      throw new NullPointerException("location is null");
    }
    setLatitude(location.getLatitude() + "");
    setLongitude(location.getLongitude() + "");
  }
}
