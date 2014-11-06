package com.avoscloud.chat.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import com.alibaba.fastjson.JSONException;
import com.avos.avoscloud.*;
import com.avoscloud.chat.avobject.User;
import com.avoscloud.chat.db.DBMsg;
import com.avoscloud.chat.entity.Conversation;
import com.avoscloud.chat.service.listener.MsgListener;
import com.avoscloud.chat.ui.activity.ChatActivity;
import com.avoscloud.chat.util.*;
import com.avoscloud.chat.avobject.ChatGroup;
import com.avoscloud.chat.base.App;
import com.avoscloud.chat.entity.Msg;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Created by lzw on 14-7-9.
 */
public class ChatService {
  private static final int REPLY_NOTIFY_ID = 1;

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
    resMsg.setType(Msg.Type.Response);
    resMsg.setToPeerId(msg.getFromPeerId());
    resMsg.setFromPeerId(getSelfId());
    resMsg.setContent("");
    resMsg.setObjectId(msg.getObjectId());
    resMsg.setRoomType(Msg.RoomType.Single);
    resMsg.setStatus(Msg.Status.SendStart);
    resMsg.setConvid(AVOSUtils.convid(getSelfId(),msg.getFromPeerId()));
    Session session = getSession();
    AVMessage avMsg = resMsg.toAVMessage();
    session.sendMessage(avMsg);
  }

  public static Msg sendAudioMsg(User toUser, String path, String msgId, Group group) throws IOException, AVException {
    return sendFileMsg(toUser, msgId, Msg.Type.Audio, path, group);
  }

  public static Msg sendImageMsg(User user, String filePath, String msgId, Group group) throws IOException, AVException {
    return sendFileMsg(user, msgId, Msg.Type.Image, filePath, group);
  }

  public static Msg sendFileMsg(User toUser, String objectId, Msg.Type type, String filePath,
                                Group group) throws IOException, AVException {
    AVFile file = AVFile.withAbsoluteLocalPath(objectId, filePath);
    file.save();
    String url = file.getUrl();
    Msg msg = createAndSendMsg(toUser, type, url, objectId, group);
    DBMsg.insertMsg(msg);
    return msg;
  }

  public static Msg sendTextMsg(User toUser, String content, Group group) {
    Msg.Type type = Msg.Type.Text;
    Msg msg = sendMsgAndInsertDB(toUser, type, content, group);
    return msg;
  }

  public static Msg createAndSendMsg(User toPeer, Msg.Type type, String content, Group group) {
    String objectId = Utils.uuid();
    return createAndSendMsg(toPeer, type, content, objectId, group);
  }

  public static Msg sendMsgAndInsertDB(User toPeer, Msg.Type type, String content, Group group) {
    Msg msg = createAndSendMsg(toPeer, type, content, group);
    DBMsg.insertMsg(msg);
    return msg;
  }

  public static Msg createAndSendMsg(User toPeer, Msg.Type type, String content, String objectId, Group group) {
    Msg msg;
    msg = new Msg();
    msg.setStatus(Msg.Status.SendStart);
    msg.setContent(content);
    msg.setTimestamp(System.currentTimeMillis());
    msg.setFromPeerId(getSelfId());
    String convid;
    if (group == null) {
      String toPeerId = ChatService.getPeerId(toPeer);
      msg.setToPeerId(toPeerId);
      msg.setRoomType(Msg.RoomType.Single);
      convid = AVOSUtils.convid(ChatService.getSelfId(), toPeerId);
    } else {
      msg.setRoomType(Msg.RoomType.Group);
      convid = group.getGroupId();
    }
    msg.setObjectId(objectId);
    msg.setConvid(convid);
    msg.setType(type);
    return sendMessage(group, msg);
  }

  public static Msg sendMessage(Group group, Msg msg) {
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

  public static Msg sendLocationMessage(User toPeer, String address, double latitude, double longtitude, Group group) {
    String content = address + "&" + latitude + "&" + longtitude;
    return sendMsgAndInsertDB(toPeer, Msg.Type.Location, content, group);
  }

  public static List<Conversation> getConversationsAndCache() throws AVException {
    List<Msg> msgs = DBMsg.getRecentMsgs(User.curUserId());
    cacheUserOrChatGroup(msgs);
    ArrayList<Conversation> conversations = new ArrayList<Conversation>();
    for (Msg msg : msgs) {
      Conversation conversation = new Conversation();
      if (msg.getRoomType() == Msg.RoomType.Single) {
        String chatUserId = msg.getChatUserId();
        conversation.toUser = App.lookupUser(chatUserId);
      } else {
        conversation.chatGroup = App.lookupChatGroup(msg.getConvid());
      }
      conversation.msg = msg;
      conversations.add(conversation);
    }
    return conversations;
  }

  public static void cacheUserOrChatGroup(List<Msg> msgs) throws AVException {
    Set<String> uncachedIds = new HashSet<String>();
    Set<String> uncachedChatGroupIds = new HashSet<String>();
    for (Msg msg : msgs) {
      if (msg.getRoomType() == Msg.RoomType.Single) {
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
    Intent intent;
    if (group == null) {
      intent = ChatActivity.getUserChatIntent(context, msg.getFromPeerId());
    } else {
      intent = ChatActivity.getGroupChatIntent(context, group.getGroupId());
    }
    //why Random().nextInt()
    //http://stackoverflow.com/questions/13838313/android-onnewintent-always-receives-same-intent
    PendingIntent pend = PendingIntent.getActivity(context, new Random().nextInt(),
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
    PreferenceMap preferenceMap = PreferenceMap.getCurUserPrefDao(context);
    if (preferenceMap.isVoiceNotify()) {
      notification.defaults |= Notification.DEFAULT_SOUND;
    }
    if (preferenceMap.isVibrateNotify()) {
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
    if (msg.getType() != Msg.Type.Response) {
      responseAndReceiveMsg(context, msg, listener, group);
    } else {
      Logger.d("onResponseMessage " + msg.getContent());
      msg.setStatus(Msg.Status.SendReceived);
      DBMsg.updateStatus(msg);
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
        if (msg.getType() == Msg.Type.Audio) {
          File file = new File(msg.getAudioPath());
          String url = msg.getContent();
          Utils.downloadFileIfNotExists(url, file);
        }
        if (group != null) {
          GroupService.cacheChatGroupIfNone(group.getGroupId());
        } else {
          String fromId = msg.getFromPeerId();
          UserService.cacheUserIfNone(fromId);
        }
      }

      @Override
      protected void onPost(Exception e) {
        if (e != null) {
          Utils.toast(context, com.avoscloud.chat.R.string.badNetwork);
        } else {
          DBMsg.insertMsg(msg);
          MsgListener _msgListener = filterMsgListener(listener, msg, group);
          if (_msgListener == null) {
            if (User.curUser() != null) {
              PreferenceMap preferenceMap = PreferenceMap.getCurUserPrefDao(context);
              if (preferenceMap.isNotifyWhenNews()) {
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
      String chatUserId= msg.getChatUserId();
      if(chatUserId.equals(listenerId)){ //may be group
        return msgListener;
      }
    }
    return null;
  }

  public static void onMessageSent(AVMessage avMsg, MsgListener listener, Group group) {
    Msg msg = Msg.fromAVMessage(avMsg);
    if (msg.getType() != Msg.Type.Response) {
      msg.setFromPeerId(User.curUserId());
      msg.setStatus(Msg.Status.SendSucceed);
      DBMsg.updateStatusAndTimestamp(msg);
      MsgListener _listener = filterMsgListener(listener, msg, group);
      if (_listener != null) {
        _listener.onMessageSent(msg);
      }
    }
  }

  public static void updateStatusToFailed(AVMessage avMsg, MsgListener msgListener) {
    Msg msg = Msg.fromAVMessage(avMsg);
    if (msg.getType() != Msg.Type.Response) {
      msg.setStatus(Msg.Status.SendFailed);
      DBMsg.updateStatus(msg);
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

  public static void resendMsg(Msg msg) {
    Group group = null;
    if (msg.getRoomType() == Msg.RoomType.Single) {
      String groupId = msg.getConvid();
      Session session = ChatService.getSession();
      group = session.getGroup(groupId);
    }
    sendMessage(group, msg);
    msg.setStatus(Msg.Status.SendStart);
    DBMsg.updateStatus(msg);
  }

  public static void cancelNotification(Context ctx) {
    Utils.cancelNotification(ctx, REPLY_NOTIFY_ID);
  }
}
