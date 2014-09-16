package com.lzw.talk.receiver;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.avos.avoscloud.AVMessage;
import com.avos.avoscloud.AVMessageReceiver;
import com.avos.avoscloud.Session;
import com.lzw.talk.activity.ChatActivity;
import com.lzw.talk.base.C;
import com.lzw.talk.db.DBMsg;
import com.lzw.talk.entity.Msg;
import com.lzw.talk.service.PrefDao;
import com.lzw.talk.service.ChatService;
import com.lzw.talk.service.MessageListener;
import com.lzw.talk.service.StatusListner;
import com.lzw.talk.util.Logger;
import com.lzw.talk.util.Utils;

import java.util.*;

/**
 * Created by lzw on 14-8-7.
 */
public class MsgReceiver extends AVMessageReceiver {
  private static final int REPLY_NOTIFY_ID = 1;
  private final Queue<String> failedMessage = new LinkedList<String>();
  PrefDao prefDao;
  public static StatusListner statusListner;
  public static Set<String> onlines = new HashSet<String>();

  @Override
  public void onSessionOpen(Context context, Session session) {
    Logger.d("onSessionOpen");
    prefDao = new PrefDao(context);
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
    Msg msg=Msg.fromAVMessage(avMsg);
    String selfId=ChatService.getSelfId();
    msg.setToPeerIds(Utils.oneToList(selfId));
    MessageListener listener = sessionMessageDispatchers.get(msg.getFromPeerId());
    if(msg.getType()==Msg.TYPE_TEXT){
      ChatService.insertDBMsg(msg);

      if (listener == null) {
        notifyMsg(context, msg);
      } else {
        Logger.d("refresh datas");
        listener.onMessage(msg);
      }
      ChatService.sendResponseMessage(msg);
    }else if(msg.getType()==Msg.TYPE_RESPONSE){
      Logger.d("receive reponse message");
      DBMsg.updateStatusAndTimestamp(msg);
      if(listener!=null){
        listener.onMessage(msg);
      }
    }
  }

  @Override
  public void onMessageSent(Context context, Session session, AVMessage msg) {
    String s = msg.getMessage();
    Logger.d("onMsgSent=" + s+" toIds="+msg.getToPeerIds());
  }

  @Override
  public void onMessageFailure(Context context, Session session, AVMessage msg) {
    String msgStr = msg.getMessage();
    failedMessage.add(msgStr);
  }

  public static void notifyMsg(Context context, Msg msg) throws JSONException {
    int icon = context.getApplicationInfo().icon;
    PendingIntent pend = PendingIntent.getActivity(context, 0,
        new Intent(context, ChatActivity.class), 0);
    Notification.Builder builder = new Notification.Builder(context);
    String content=msg.getContent();
    builder.setContentIntent(pend)
        .setSmallIcon(icon)
        .setWhen(System.currentTimeMillis())
        .setTicker(content)
        .setContentTitle("新的消息")
        .setContentText(content)
        .setAutoCancel(true);
    NotificationManager man = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    man.notify(REPLY_NOTIFY_ID, builder.getNotification());
  }

  @Override
  public void onStatusOnline(Context context, Session session, List<String> strings) {
    Logger.d("onStatusOnline " + strings);
    onlines.addAll(strings);
    statusListner.onStatusOnline(new ArrayList<String>(onlines));
  }

  @Override
  public void onStatusOffline(Context context, Session session, List<String> strings) {
    Logger.d("onStatusOff " + strings);
    onlines.removeAll(strings);
    statusListner.onStatusOnline(new ArrayList<String>(onlines));
  }

  @Override
  public void onError(Context context, Session session, Throwable throwable) {
    throwable.printStackTrace();
    Logger.d(throwable.getMessage());
    Logger.d("onError");
  }

  public static void registerSessionListener(String peerId, MessageListener listener) {
    sessionMessageDispatchers.put(peerId, listener);
  }

  public static void unregisterSessionListener(String peerId) {
    sessionMessageDispatchers.remove(peerId);
  }

  static HashMap<String, MessageListener> sessionMessageDispatchers =
      new HashMap<String, MessageListener>();

  public static void registerSatusListener(StatusListner listener) {
    statusListner = listener;
  }

  public static void unregisterSatutsListener() {
    statusListner = null;
  }

  public static List<String> getOnlines() {
    return new ArrayList<String>(onlines);
  }
}
