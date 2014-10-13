package com.lzw.talk.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import com.alibaba.fastjson.JSONException;
import com.avos.avoscloud.*;
import com.lzw.talk.avobject.ChatGroup;
import com.lzw.talk.avobject.User;
import com.lzw.talk.base.App;
import com.lzw.talk.db.DBMsg;
import com.lzw.talk.entity.Msg;
import com.lzw.talk.entity.RecentMsg;
import com.lzw.talk.ui.activity.ChatActivity;
import com.lzw.talk.util.AVOSUtils;
import com.lzw.talk.util.Logger;
import com.lzw.talk.util.NetAsyncTask;
import com.lzw.talk.util.Utils;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Created by lzw on 14-7-9.
 */
public class ChatService {
  private static final int REPLY_NOTIFY_ID = 1;

  public static List<User> findChatUsers() throws AVException {
    List<User> users = getAllUsers();
    App.registerBatchUserCache(users);
    users.remove(User.curUser());
    return users;
  }

  public static List<User> getAllUsers() throws AVException {
    AVQuery<User> q = AVUser.getQuery(User.class);
    return q.find();
  }

  public static <T extends AVUser> String getPeerId(T user) {
    return user.getObjectId();
  }

  public static String getSelfId() {
    return getPeerId(User.curUser());
  }

  public static <T extends AVUser> void withUsersToWatch(List<T> users, boolean watch) {
    List<String> peerIds = new ArrayList<String>();
    for (AVUser user : users) {
      peerIds.add(getPeerId(user));
    }
    String selfId = getPeerId(User.curUser());
    Session session = SessionManager.getInstance(selfId);
    if (watch) {
      session.watchPeers(peerIds);
    }
  }

  public static <T extends AVUser> void withUserToWatch(T user, boolean watch) {
    List<T> users = new ArrayList<T>();
    users.add(user);
    withUsersToWatch(users, watch);
  }

  public static Session getSession() {
    return SessionManager.getInstance(getPeerId(User.curUser()));
  }

  public static void sendResponseMessage(Msg msg) {
    Msg resMsg = new Msg();
    resMsg.setType(Msg.TYPE_RESPONSE);
    resMsg.setToPeerId(msg.getFromPeerId());
    resMsg.setFromPeerId(getSelfId());
    resMsg.setContent(msg.getTimestamp() + "");
    resMsg.setObjectId(msg.getObjectId());
    Session session = getSession();
    AVMessage avMsg = resMsg.toAVMessage();
    session.sendMessage(avMsg);
  }

  public static Msg sendAudioMsg(User toUser, String path, String msgId, Group group) throws IOException, AVException {
    return sendFileMsg(toUser, msgId, Msg.TYPE_AUDIO, path, group);
  }

  public static Msg sendImageMsg(User user, String filePath, String msgId, Group group) throws IOException, AVException {
    return sendFileMsg(user, msgId, Msg.TYPE_IMAGE, filePath, group);
  }

  public static Msg sendFileMsg(User toUser, String objectId, int type, String filePath, Group group) throws IOException, AVException {
    AVFile file = AVFile.withAbsoluteLocalPath(objectId, filePath);
    file.save();
    String url = file.getUrl();
    String sendText = filePath + "&" + url;
    Msg msg = sendMessage(toUser, type, sendText, objectId, group);
    DBMsg.insertMsg(msg, group);
    return msg;
  }

  public static Msg sendTextMsg(User toUser, String content, Group group) {
    int type = Msg.TYPE_TEXT;
    Msg msg = sendMessage(toUser, type, content, group);
    Log.i("lzw", "sendTextMsg fromId=" + msg.getFromPeerId() + " toId=" + msg.getToPeerId());
    DBMsg.insertMsg(msg, group);
    return msg;
  }

  public static Msg sendMessage(User toPeer, int type, String content, Group group) {
    String objectId = Utils.uuid();
    return sendMessage(toPeer, type, content, objectId, group);
  }

  public static Msg sendMessage(User toPeer, int type, String content, String objectId, Group group) {
    Msg msg;
    msg = new Msg();
    msg.setStatus(Msg.STATUS_SEND_START);
    msg.setContent(content);
    msg.setTimestamp(System.currentTimeMillis());
    msg.setFromPeerId(getSelfId());
    String convid;
    if (group == null) {
      String toPeerId = ChatService.getPeerId(toPeer);
      msg.setToPeerId(toPeerId);
      msg.setSingleChat(true);
      convid = AVOSUtils.convid(ChatService.getSelfId(), toPeerId);
    } else {
      msg.setSingleChat(false);
      convid = group.getGroupId();
    }
    msg.setObjectId(objectId);
    msg.setConvid(convid);
    msg.setType(type);

    AVMessage avMsg = msg.toAVMessage();
    Session session = getSession();
    if (group == null) {
      session.sendMessage(avMsg);
    } else {
      group.sendMessage(avMsg);
    }
    return msg;
  }

  public static void openSession() {
    Session session = getSession();
    if (session.isOpen() == false) {
      session.open(new LinkedList<String>());
    }
  }

  public static HashMap<String, String> parseUri(String uri) {
    String[] parts = uri.split("&");
    HashMap<String, String> map = new HashMap<String, String>();
    map.put("path", parts[0]);
    map.put("url", parts[1]);
    return map;
  }

  public static Msg sendLocationMessage(User toPeer, String address, double latitude, double longtitude, Group group) {
    String content = address + "&" + latitude + "&" + longtitude;
    Logger.d("content=" + content);
    Msg msg = sendMessage(toPeer, Msg.TYPE_LOCATION, content, group);
    DBMsg.insertMsg(msg, group);
    return msg;
  }

  public static List<RecentMsg> getRecentMsgsAndCache() throws AVException {
    List<Msg> msgs = DBMsg.getRecentMsgs();
    cacheUserOrChatGroup(msgs);
    ArrayList<RecentMsg> recentMsgs = new ArrayList<RecentMsg>();
    for (Msg msg : msgs) {
      RecentMsg recentMsg = new RecentMsg();
      if (msg.isSingleChat()) {
        String chatUserId = msg.getChatUserId();
        recentMsg.toUser = App.lookupUser(chatUserId);
      } else {
        recentMsg.chatGroup = App.lookupChatGroup(msg.getConvid());
      }
      recentMsg.msg = msg;
      recentMsgs.add(recentMsg);
    }
    return recentMsgs;
  }

  public static void cacheUserOrChatGroup(List<Msg> msgs) throws AVException {
    Set<String> uncachedIds = new HashSet<String>();
    Set<String> uncachedChatGroupIds = new HashSet<String>();
    for (Msg msg : msgs) {
      if (msg.isSingleChat()) {
        String chatUserId = msg.getChatUserId();
        if (App.lookupUser(chatUserId) == null) {
          uncachedIds.add(chatUserId);
        }
      } else {
        String groupId = msg.getConvid();
        if (App.lookupChatGroup(groupId) == null) {
          uncachedChatGroupIds.add(groupId);
        }
      }
    }
    UserService.cacheUser(new ArrayList<String>(uncachedIds));
    GroupService.cacheChatGroups(new ArrayList<String>(uncachedChatGroupIds));
  }

  public static void closeSession() {
    Session session = ChatService.getSession();
    session.close();
  }

  public static Group getGroupById(String groupId) {
    return getSession().getGroup(groupId);
  }

  public static void notifyMsg(Context context, Msg msg, Group group) throws JSONException {
    int icon = context.getApplicationInfo().icon;
    Intent intent = new Intent(context, ChatActivity.class);
    if (group == null) {
      intent.putExtra(ChatActivity.CHAT_USER_ID, msg.getFromPeerId());
    } else {
      intent.putExtra(ChatActivity.GROUP_ID, group.getGroupId());
      intent.putExtra(ChatActivity.SINGLE_CHAT, false);
    }
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

  public static void onMessage(Context context, AVMessage avMsg, MsgListener listener, Group group) {
    final Msg msg = Msg.fromAVMessage(avMsg);
    if (group == null) {
      String selfId = getSelfId();
      msg.setToPeerId(selfId);
    }
    if (msg.getType() != Msg.TYPE_RESPONSE) {
      responseAndReceiveMsg(context, msg, listener, group);
    } else {
      DBMsg.updateStatusAndTimestamp(msg);
      MsgListener _listener = filterMsgListener(listener, msg, group);
      if (_listener != null) {
        _listener.onMessage(msg);
      }
    }
  }

  public static void responseAndReceiveMsg(final Context context, final Msg msg, final MsgListener listener, final Group group) {
    if (group == null) {
      sendResponseMessage(msg);
    }
    new NetAsyncTask(context, false) {
      @Override
      protected void doInBack() throws Exception {
        if (msg.getType() == Msg.TYPE_AUDIO) {
          File file = new File(msg.getAudioPath());
          String uri = msg.getContent();
          Map<String, String> parts = parseUri(uri);
          String url = parts.get("url");
          Utils.downloadFileIfNotExists(url, file);
        }
        String fromId = msg.getFromPeerId();
        App.cacheUserIfNot(fromId);
      }

      @Override
      protected void onPost(Exception e) {
        if (e != null) {
          Utils.toast(context, com.lzw.talk.R.string.badNetwork);
        } else {
          DBMsg.insertMsg(msg, group);
          MsgListener _msgListener = filterMsgListener(listener, msg, group);
          if (_msgListener == null) {
            if (User.curUser() != null) {
              PrefDao prefDao = PrefDao.getCurUserPrefDao(context);
              if (prefDao.isNotifyWhenNews()) {
                notifyMsg(context, msg, group);
              }
            }
          } else {
            listener.onMessage(msg);
          }
        }
      }


    }.execute();
  }

  public static MsgListener filterMsgListener(MsgListener msgListener, Msg msg, Group group) {
    if (msgListener != null) {
      String listenerId = msgListener.getListenerId();
      if (group == null) {
        String chatUserId = msg.getChatUserId();
        if (chatUserId.equals(listenerId)) {
          return msgListener;
        }
      } else {
        if (group.getGroupId().equals(listenerId)) {
          return msgListener;
        }
      }
    }
    return null;
  }

  public static void onMessageSent(AVMessage avMsg, MsgListener listener, Group group) {
    Msg msg = Msg.fromAVMessage(avMsg);
    if (msg.getType() != Msg.TYPE_RESPONSE) {
      msg.setStatus(Msg.STATUS_SEND_SUCCEED);
      DBMsg.updateStatusToSendSucceed(msg);
      msg.setFromPeerId(User.curUserId());
      MsgListener _listener = filterMsgListener(listener, msg, group);
      if (_listener != null) {
        _listener.onMessageSent(msg);
      }
    }
  }

  public static void updateStatusToFailed(AVMessage avMsg, MsgListener msgListener) {
    Msg msg = Msg.fromAVMessage(avMsg);
    if (msg.getType() != Msg.TYPE_RESPONSE) {
      msg.setStatus(Msg.STATUS_SEND_FAILED);
      DBMsg.updateStatusToSendFailed(msg);
      if (msgListener != null) {
        msgListener.onMessageFailure(msg);
      }
    }
  }

  public static void onMessageError(Throwable throwable, MsgListener msgListener) {
    String errorMsg = throwable.getMessage();
    Logger.d("error " + errorMsg);
    if (errorMsg != null && errorMsg.startsWith("{")) {
      AVMessage avMsg = new AVMessage(errorMsg);
      updateStatusToFailed(avMsg, msgListener);
    }
  }

  public static List<User> findGroupMembers(ChatGroup chatGroup) throws AVException {
    List<String> members = chatGroup.getMembers();
    return UserService.findUsers(members);
  }

}
