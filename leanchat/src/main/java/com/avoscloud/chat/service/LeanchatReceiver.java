package com.avoscloud.chat.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.avoscloud.chat.R;
import com.avoscloud.chat.service.event.InvitationEvent;
import com.avoscloud.chat.ui.entry.EntrySplashActivity;
import com.avoscloud.chat.util.NotificationUtil;
import com.avoscloud.chat.util.StringUtils;

import org.json.JSONException;
import org.json.JSONObject;

import de.greenrobot.event.EventBus;

/**
 * Created by wli on 15/7/10.
 */
public class LeanchatReceiver extends BroadcastReceiver {

  public final static String AVOS_DATA = "com.avoscloud.Data";

  @Override
  public void onReceive(Context context, Intent intent) {
    String action = intent.getAction();
    if (!StringUtils.isEmpty(action)) {
      if (action.equals(context.getString(R.string.invitation_action))) {
        String avosData = intent.getStringExtra(AVOS_DATA);
        if (!StringUtils.isEmpty(avosData)) {
          try {
            JSONObject json = new JSONObject(avosData);
            if (null != json) {
              String alertStr = json.getString(PushManager.AVOS_ALERT);
              NotificationUtil.showNotification(context, "LeanChat", alertStr, EntrySplashActivity.class);
            }
          } catch (JSONException e) {
            e.printStackTrace();
          }
        }
      }
    }
    EventBus.getDefault().post(new InvitationEvent());
  }
}