package com.avoscloud.leanchatlib.activity;

import android.app.Activity;
import android.content.Intent;

/**
 * Created by lzw on 15/4/27.
 */
public interface LocationHandler {
  public void selectLocationByRequestCode(Activity activity, int requestCode);

  public void seeLocationDetail(Activity activity, double latitude, double longitude);

  public void handleLocationResultIntent(Intent intent);
}
