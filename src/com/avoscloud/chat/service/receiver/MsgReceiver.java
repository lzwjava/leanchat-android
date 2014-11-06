package com.avoscloud.chat.service.receiver;

import android.content.Context;
import com.avos.avoscloud.AVMessage;
import com.avos.avoscloud.AVMessageReceiver;
import com.avos.avoscloud.Session;
import com.avoscloud.chat.service.ChatService;
import com.avoscloud.chat.service.listener.MsgListener;
import com.avoscloud.chat.service.listener.StatusListener;
import com.avoscloud.chat.util.Logger;

import java.util.*;

/**
 * Created by lzw on 14-8-7.
 */
public class MsgReceiver extends AVMessageReceiver {
  public static StatusListener statusListener;
  public static Set<String> onlines = new HashSet<String>();
  public static MsgListener msgListener;

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
  }

  @Override
  public void onMessage(final Context context, Session session, AVMessage avMsg) {
    Logger.d("onMessage");
    ChatService.onMessage(context, avMsg, msgListener, null);
  }

  @Override
  public void onMessageSent(Context context, Session session, AVMessage avMsg) {
    Logger.d("onMessageSent " + avMsg.getMessage());
    Logger.d("timestamp "+avMsg.getTimestamp());
    ChatService.onMessageSent(avMsg, msgListener, null);
  }

  @Override
  public void onMessageFailure(Context context, Session session, AVMessage avMsg) {
    ChatService.updateStatusToFailed(avMsg, msgListener);
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
    ChatService.onMessageError(throwable, msgListener);
  }

  public static void registerStatusListener(StatusListener listener) {
    statusListener = listener;
  }

  public static void unregisterSatutsListener() {
    statusListener = null;
  }

  public static void registerMsgListener(MsgListener listener) {
    msgListener = listener;
  }

  public static void unregisterMsgListener() {
    msgListener = null;
  }

  public static List<String> getOnlines() {
    return new ArrayList<String>(onlines);
  }
}
