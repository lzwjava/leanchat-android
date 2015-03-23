package com.avoscloud.chat.service;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

/**
 * Created by lzw on 14/11/20.
 */
public class LoginFinishReceiver extends BroadcastReceiver {
  private static final String FINISH_ACTION = "com.avoscloud.chat_finish";
  private Activity activity;

  public LoginFinishReceiver(Activity activity) {
    this.activity = activity;
  }

  public static LoginFinishReceiver register(Activity activity) {
    LoginFinishReceiver receiver = new LoginFinishReceiver(activity);
    activity.registerReceiver(receiver, new IntentFilter(LoginFinishReceiver.FINISH_ACTION));
    return receiver;
  }

  public static void broadcast(Context context) {
    context.sendBroadcast(new Intent(FINISH_ACTION));
  }

  @Override
  public void onReceive(Context context, Intent intent) {
    if (intent != null && intent.getAction().equals(FINISH_ACTION)) {
      activity.finish();
    }
  }
}
