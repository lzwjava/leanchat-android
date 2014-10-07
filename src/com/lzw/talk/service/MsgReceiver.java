package com.lzw.talk.service;

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
import com.lzw.talk.avobject.User;
import com.lzw.talk.base.App;
import com.lzw.talk.db.DBMsg;
import com.lzw.talk.entity.Msg;
import com.lzw.talk.ui.activity.ChatActivity;
import com.lzw.talk.util.Logger;
import com.lzw.talk.util.NetAsyncTask;
import com.lzw.talk.util.Utils;

import java.io.File;
import java.util.*;

/**
 * Created by lzw on 14-8-7.
 */
public class MsgReceiver extends AVMessageReceiver {
  private static final int REPLY_NOTIFY_ID = 1;
  private final Queue<String> failedMessage = new LinkedList<String>();
  public static StatusListener statusListener;
  public static Set<String> onlines = new HashSet<String>();
  public static Map<String, MessageListener> messageListeners
      = new HashMap<String, MessageListener>();

  @Override
  public void onSessionOpen(Context context, Session session) {
    Logger.d("onSessionOpen");
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
  public void onMessage(final Context context, Session session, AVMessage avMsg) {
    Logger.d("onMessage");
    final Msg msg = Msg.fromAVMessage(avMsg);
    String selfId = ChatService.getSelfId();
    msg.setToPeerIds(Arrays.asList(selfId));
    if (msg.getType() != Msg.TYPE_RESPONSE) {
      responseAndReceiveMsg(context, msg);
    } else {
      DBMsg.updateStatusAndTimestamp(msg);
      MessageListener messageListener = getMessageListener(messageListeners, msg);
      if (messageListener != null) {
        messageListener.onMessage(msg);
      }
    }
  }

  public void responseAndReceiveMsg(final Context context, final Msg msg) {
    ChatService.sendResponseMessage(msg);
    new NetAsyncTask(context, false) {
      @Override
      protected void doInBack() throws Exception {
        if (msg.getType() == Msg.TYPE_AUDIO) {
          File file = new File(msg.getAudioPath());
          String uri = msg.getContent();
          Map<String, String> parts = ChatService.parseUri(uri);
          String url = parts.get("url");
          Utils.downloadFileIfNotExists(url, file);
        }
        String fromId = msg.getFromPeerId();
        App.cacheUserIfNot(fromId);
      }

      @Override
      protected void onPost(Exception e) {
        if (e != null) {
          Utils.toast(context, R.string.badNetwork);
        } else {
          DBMsg.insertMsg(msg);
          MessageListener listener = getMessageListener(messageListeners, msg);
          if (listener == null) {
            if (User.curUser() != null) {
              PrefDao prefDao = PrefDao.getCurUserPrefDao(context);
              if (prefDao.isNotifyWhenNews()) {
                notifyMsg(context, msg);
              }
            }
          } else {
            listener.onMessage(msg);
          }
        }
      }


    }.execute();
  }

  private MessageListener getMessageListener(Map<String, MessageListener> listeners, Msg msg) {
    String chatUserId = msg.getChatUserId();
    if (chatUserId == null) {
      throw new NullPointerException("fromPeerId is null");
    } else {
      return listeners.get(chatUserId);
    }
  }

  @Override
  public void onMessageSent(Context context, Session session, AVMessage avMsg) {
    Logger.d("onMessageSent " + avMsg.getToPeerIds());
    Msg msg = Msg.fromAVMessage(avMsg);
    if (msg.getType() != Msg.TYPE_RESPONSE) {
      msg.setStatus(Msg.STATUS_SEND_SUCCEED);
      DBMsg.updateStatusToSendSucceed(msg);
      String toPeerId = avMsg.getToPeerIds().get(0);
      MessageListener listener = messageListeners.get(toPeerId);
      if (listener != null) {
        listener.onMessageSent(msg);
      }
    }
  }

  @Override
  public void onMessageFailure(Context context, Session session, AVMessage avMsg) {
    updateStatusToFailed(avMsg);
  }

  public void updateStatusToFailed(AVMessage avMsg) {
    Msg msg = Msg.fromAVMessage(avMsg);
    if (msg.getType() != Msg.TYPE_RESPONSE) {
      msg.setStatus(Msg.STATUS_SEND_FAILED);
      DBMsg.updateStatusToSendFailed(msg);
      for (Map.Entry<String, MessageListener> entry : messageListeners.entrySet()) {
        MessageListener listener = entry.getValue();
        if (listener != null) {
          listener.onMessageFailure(msg);
        }
      }
    }
  }

  public static void notifyMsg(Context context, Msg msg) throws JSONException {
    int icon = context.getApplicationInfo().icon;
    Intent intent = new Intent(context, ChatActivity.class);
    intent.putExtra(ChatActivity.CHAT_USER_ID, msg.getFromPeerId());
    PendingIntent pend = PendingIntent.getActivity(context, 0,
        intent, 0);
    Notification.Builder builder = new Notification.Builder(context);
    CharSequence notifyContent = msg.getNotifyContent();
    CharSequence username = msg.getFromName();
    builder.setContentIntent(pend)
        .setSmallIcon(icon)
        .setWhen(System.currentTimeMillis())
        .setTicker(username + "\n" + notifyContent)
        .setContentTitle(username)
        .setContentText(notifyContent)
        .setAutoCancel(true);
    NotificationManager man = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    Notification notification = builder.getNotification();
    PrefDao prefDao = PrefDao.getCurUserPrefDao(context);
    if (prefDao.isVoiceNotify()) {
      notification.defaults |= Notification.DEFAULT_SOUND;
    }
    if (prefDao.isVibrateNotify()) {
      notification.defaults |= Notification.DEFAULT_VIBRATE;
    }
    man.notify(REPLY_NOTIFY_ID, notification);
  }

  @Override
  public void onStatusOnline(Context context, Session session, List<String> strings) {
    Logger.d("onStatusOnline " + strings);
    onlines.addAll(strings);
    if (statusListener != null) {
      statusListener.onStatusOnline(new ArrayList<String>(onlines));
    }
  }

  @Override
  public void onStatusOffline(Context context, Session session, List<String> strings) {
    Logger.d("onStatusOff " + strings);
    onlines.removeAll(strings);
    if (statusListener != null) {
      statusListener.onStatusOnline(new ArrayList<String>(onlines));
    }
  }

  @Override
  public void onError(Context context, Session session, Throwable throwable) {
    throwable.printStackTrace();

    String errorMsg = throwable.getMessage();
    Logger.d("error " + errorMsg);
    if (errorMsg != null && errorMsg.startsWith("{")) {
      AVMessage avMsg = new AVMessage(errorMsg);
      updateStatusToFailed(avMsg);
    }
  }

  public static void registerMessageListener(String chatUserId, MessageListener listener) {
    messageListeners.put(chatUserId, listener);
  }

  public static void unregisterMessageListener(String chatUserId) {
    messageListeners.put(chatUserId, null);
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
