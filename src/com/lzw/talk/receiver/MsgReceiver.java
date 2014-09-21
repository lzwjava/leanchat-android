package com.lzw.talk.receiver;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import com.alibaba.fastjson.JSONException;
import com.avos.avoscloud.AVMessage;
import com.avos.avoscloud.AVMessageReceiver;
import com.avos.avoscloud.Session;
import com.lzw.talk.R;
import com.lzw.talk.base.App;
import com.lzw.talk.db.DBMsg;
import com.lzw.talk.entity.Msg;
import com.lzw.talk.service.ChatService;
import com.lzw.talk.service.MessageListener;
import com.lzw.talk.service.PrefDao;
import com.lzw.talk.service.StatusListener;
import com.lzw.talk.util.Logger;
import com.lzw.talk.util.Utils;
import com.lzw.talk.view.activity.ChatActivity;
import com.lzw.talk.view.activity.MainActivity;

import java.util.*;

/**
 * Created by lzw on 14-8-7.
 */
public class MsgReceiver extends AVMessageReceiver {
  private static final int REPLY_NOTIFY_ID = 1;
  private final Queue<String> failedMessage = new LinkedList<String>();
  PrefDao prefDao;
  public static StatusListener statusListener;
  public static Set<String> onlines = new HashSet<String>();
  public static MessageListener messageListener;

  @Override
  public void onSessionOpen(Context context, Session session) {
    Logger.d("onSessionOpen");
    prefDao = new PrefDao(context);
    goMainActivity(context);
  }

  public void goMainActivity(Context context) {
    Intent intent = new Intent(context, MainActivity.class);
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
    context.startActivity(intent);
  }

  @Override
  public void onSessionPaused(Context context, Session session) {
    Logger.d("onSessionPaused");
  }

  @Override
  public void onSessionResumed(Context context, Session session) {
    Logger.d("onSessionResumed");
    while (!failedMessage.isEmpty()) {
      String msg = failedMessage.poll();
      session.sendMessage(msg, session.getAllPeers(), false);
    }
  }

  @Override
  public void onMessage(Context context, Session session, AVMessage avMsg) {
    Logger.d("onMessage");
    Msg msg = Msg.fromAVMessage(avMsg);
    String selfId = ChatService.getSelfId();
    msg.setToPeerIds(Utils.oneToList(selfId));
    if (msg.getType() == Msg.TYPE_TEXT || msg.getType() == Msg.TYPE_IMAGE) {
      ChatService.insertDBMsg(msg);
      if (messageListener == null) {
        notifyMsg(context, msg);
      } else {
        Logger.d("refresh datas");
        messageListener.onMessage(msg);
      }
      ChatService.sendResponseMessage(msg);
    } else if (msg.getType() == Msg.TYPE_RESPONSE) {
      DBMsg.updateStatusAndTimestamp(msg);
      if (messageListener != null) {
        messageListener.onMessage(msg);
      }
    }
  }

  @Override
  public void onMessageSent(Context context, Session session, AVMessage avMsg) {
    Logger.d("onMessageSent");
    Msg msg = Msg.fromAVMessage(avMsg);
    if (msg.getType() == Msg.TYPE_TEXT || msg.getType() == Msg.TYPE_IMAGE) {
      DBMsg.updateStatusToSendSucceed(msg);
      if (messageListener != null) {
        messageListener.onMessageSent(msg);
      }
    }
  }

  @Override
  public void onMessageFailure(Context context, Session session, AVMessage avMsg) {
    Msg msg = Msg.fromAVMessage(avMsg);
    Logger.d("onMessageFailure");
    if (msg.getType() == Msg.TYPE_TEXT || msg.getType() == Msg.TYPE_IMAGE) {
      DBMsg.updateStatusToSendFailed(msg);
      if (messageListener != null) {
        messageListener.onMessageSent(msg);
      }
    }
  }

  public static void notifyMsg(Context context, Msg msg) throws JSONException {
    int icon = context.getApplicationInfo().icon;
    PendingIntent pend = PendingIntent.getActivity(context, 0,
        new Intent(context, ChatActivity.class), 0);
    Notification.Builder builder = new Notification.Builder(context);
    String content = msg.getContent();
    if (msg.getType() == Msg.TYPE_IMAGE) {
      content = App.ctx.getString(R.string.image);
    }
    builder.setContentIntent(pend)
        .setSmallIcon(icon)
        .setWhen(System.currentTimeMillis())
        .setTicker(content)
        .setContentTitle(App.ctx.getString(R.string.newMessage))
        .setContentText(content)
        .setAutoCancel(true);
    NotificationManager man = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    man.notify(REPLY_NOTIFY_ID, builder.getNotification());
  }

  @Override
  public void onStatusOnline(Context context, Session session, List<String> strings) {
    Logger.d("onStatusOnline " + strings);
    onlines.addAll(strings);
    statusListener.onStatusOnline(new ArrayList<String>(onlines));
  }

  @Override
  public void onStatusOffline(Context context, Session session, List<String> strings) {
    Logger.d("onStatusOff " + strings);
    onlines.removeAll(strings);
    statusListener.onStatusOnline(new ArrayList<String>(onlines));
  }

  @Override
  public void onError(Context context, Session session, Throwable throwable) {
    throwable.printStackTrace();
    String errorMsg = throwable.getMessage();
    if (errorMsg != null && errorMsg.startsWith("{")) {
      AVMessage avMsg = new AVMessage(errorMsg);
      Msg msg = Msg.fromAVMessage(avMsg);
      DBMsg.updateStatusToSendFailed(msg);
      if (messageListener != null) {
        messageListener.onMessageFailure(msg);
      }
    }
    //if error is Session is Already open ,we still go next Activity.
    if (errorMsg != null && errorMsg.equals("Session is already opened")) {
      goMainActivity(context);
    }
    Logger.d("error "+errorMsg);
  }

  public static void registerMessageListener(MessageListener listener) {
    messageListener = listener;
  }

  public static void unregisterMessageListener() {
    messageListener = null;
  }

  public static void registerStatusListener(StatusListener listener) {
    statusListener = listener;
  }

  public static void unregisterSatutsListener() {
    statusListener = null;
  }

  public static List<String> getOnlines() {
    return new ArrayList<String>(onlines);
  }
}
