package com.avoscloud.leanchatlib.activity;

import android.app.Activity;
import com.avos.avoscloud.im.v2.messages.AVIMLocationMessage;

/**
 * Created by lzw on 15/4/27.
 */
public interface LocationHandler {
  public void onAddLocationButtonClicked(Activity activity);

  public void onLocationMessageViewClicked(Activity activity, AVIMLocationMessage locationMessage);
}
