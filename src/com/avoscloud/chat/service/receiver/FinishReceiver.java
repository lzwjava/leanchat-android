package com.avoscloud.chat.service.receiver;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

/**
 * Created by lzw on 14/11/20.
 */
public class FinishReceiver extends BroadcastReceiver {
  private static final String FINISH_ACTION = "com.avoscloud.chat_finish";
  private Activity activity;

  public FinishReceiver(Activity activity) {
    this.activity = activity;
  }

  @Override
  public void onReceive(Context context, Intent intent) {
    if (intent != null && intent.getAction().equals(FINISH_ACTION)) {
      activity.finish();
    }
  }

  public static FinishReceiver register(Activity activity) {
    FinishReceiver receiver = new FinishReceiver(activity);
    activity.registerReceiver(receiver, new IntentFilter(FinishReceiver.FINISH_ACTION));
    return receiver;
  }

  public static void broadcast(Context context) {
    context.sendBroadcast(new Intent(FINISH_ACTION));
  }
}
