package com.avoscloud.chat.util;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.avos.avospush.notification.NotificationCompat;

import java.util.Random;

/**
 * Created by wli on 15/7/15.
 */
public class NotificationUtil {

  public static void showNotification(Context context, String title, String content, Class<?> className) {
    showNotification(context, title, content, null, className);
  }

  public static void showNotification(Context context, String title, String content, String sound, Class<?> className) {

    int notificationId = (new Random()).nextInt();
    Intent intent = new Intent();
    ComponentName cn = new ComponentName(context, className);
    intent.setComponent(cn);
    PendingIntent contentIntent = PendingIntent.getActivity(context, notificationId, intent, 0);
    NotificationCompat.Builder mBuilder =
      new NotificationCompat.Builder(context)
        .setSmallIcon(context.getApplicationInfo().icon)
        .setContentTitle(title).setAutoCancel(true).setContentIntent(contentIntent)
        .setDefaults(Notification.DEFAULT_VIBRATE | Notification.DEFAULT_SOUND)
        .setContentText(content);
    NotificationManager manager =
      (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    Notification notification = mBuilder.build();
    if (sound != null && sound.trim().length() > 0) {
      notification.sound = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + sound);
    }
    manager.notify(notificationId, notification);
  }
}
