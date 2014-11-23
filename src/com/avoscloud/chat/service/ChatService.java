package com.avoscloud.chat.service;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import com.alibaba.fastjson.JSONException;
import com.avos.avoscloud.*;
import com.avoscloud.chat.avobject.ChatGroup;
import com.avoscloud.chat.avobject.User;
import com.avoscloud.chat.base.App;
import com.avoscloud.chat.db.DBMsg;
import com.avoscloud.chat.entity.Conversation;
import com.avoscloud.chat.entity.Msg;
import com.avoscloud.chat.entity.RoomType;
import com.avoscloud.chat.service.listener.MsgListener;
import com.avoscloud.chat.service.receiver.MsgReceiver;
import com.avoscloud.chat.ui.activity.ChatActivity;
import com.avoscloud.chat.util.AVOSUtils;
import com.avoscloud.chat.util.Logger;
import com.avoscloud.chat.util.NetAsyncTask;
import com.avoscloud.chat.util.Utils;

import java.io.File;
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
    } else {
      session.unwatchPeers(peerIds);
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

  public static void openSession(Activity activity) {
    Session session = getSession();
    session.setSignatureFactory(new SignatureFactory());
    if (session.isOpen() == false) {
      session.open(new LinkedList<String>());
    }
  }

  public static List<Conversation> getConversationsAndCache() throws AVException {
    List<Msg> msgs = DBMsg.getRecentMsgs(User.curUserId());
    cacheUserOrChatGroup(msgs);
    ArrayList<Conversation> conversations = new ArrayList<Conversation>();
    for (Msg msg : msgs) {
      Conversation conversation = new Conversation();
      if (msg.getRoomType() == RoomType.Single) {
        String chatUserId = msg.getOtherId();
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
      if (msg.getRoomType() == RoomType.Single) {
        String chatUserId = msg.getOtherId();
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

  public static void onMessage(Context context, AVMessage avMsg, Set<MsgListener> listeners, Group group) {
    final Msg msg = Msg.fromAVMessage(avMsg);
    String convid;
    if (group == null) {
      String selfId = getSelfId();
      msg.setToPeerId(selfId);
      convid = AVOSUtils.convid(selfId, msg.getFromPeerId());
      msg.setRoomType(RoomType.Single);
    } else {
      convid = group.getGroupId();
      msg.setRoomType(RoomType.Group);
    }
    msg.setStatus(Msg.Status.SendReceived);
    msg.setConvid(convid);
    handleReceivedMsg(context, msg, listeners, group);
  }

  public static void handleReceivedMsg(final Context context, final Msg msg, final Set<MsgListener> listeners, final Group group) {
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
          String otherId = getOtherId(msg.getFromPeerId(), group);
          boolean done = false;
          for (MsgListener listener : listeners) {
            if (listener.onMessageUpdate(otherId)) {
              done = true;
              break;
            }
          }
          if (!done) {
            if (User.curUser() != null) {
              PreferenceMap preferenceMap = PreferenceMap.getCurUserPrefDao(context);
              if (preferenceMap.isNotifyWhenNews()) {
                notifyMsg(context, msg, group);
              }
            }
          }
        }
      }
    }.execute();
  }

  private static String getOtherId(String otherId, Group group) {
    assert otherId != null || group != null;
    if (group != null) {
      return group.getGroupId();
    } else {
      return otherId;
    }
  }

  public static void onMessageSent(AVMessage avMsg, Set<MsgListener> listeners, Group group) {
    Msg msg = Msg.fromAVMessage(avMsg);
    DBMsg.updateStatusAndTimestamp(msg.getObjectId(), Msg.Status.SendSucceed, msg.getTimestamp());
    String otherId = getOtherId(msg.getToPeerId(), group);
    for (MsgListener msgListener : listeners) {
      if (msgListener.onMessageUpdate(otherId)) {
        break;
      }
    }
  }

  public static void onMessageFailure(AVMessage avMsg, Set<MsgListener> msgListeners, Group group) {
    Msg msg = Msg.fromAVMessage(avMsg);
    DBMsg.updateStatus(msg.getObjectId(), Msg.Status.SendFailed);
    String otherId = getOtherId(msg.getToPeerId(), group);
    for (MsgListener msgListener : msgListeners) {
      if (msgListener.onMessageUpdate(otherId)) {
        break;
      }
    }
  }

  public static void onMessageError(Throwable throwable, Set<MsgListener> msgListeners) {
    String errorMsg = throwable.getMessage();
    Logger.d("error " + errorMsg);
    if (errorMsg != null && errorMsg.startsWith("{")) {
      AVMessage avMsg = new AVMessage(errorMsg);
      //onMessageFailure(avMsg, msgListeners, group);
    }
  }

  public static List<User> findGroupMembers(ChatGroup chatGroup) throws AVException {
    List<String> members = chatGroup.getMembers();
    return UserService.findUsers(members);
  }

  public static Msg resendMsg(Msg msg) {
    String toId;
    if (msg.getRoomType() == RoomType.Group) {
      String groupId = msg.getConvid();
      toId = groupId;
    } else {
      toId = msg.getToPeerId();
      msg.setRequestReceipt(true);
    }
    MsgAgent msgAgent = new MsgAgent(msg.getRoomType(), toId);
    msgAgent.sendMsg(msg);
    DBMsg.updateStatus(msg.getObjectId(), Msg.Status.SendStart);
    return msg;
  }

  public static void cancelNotification(Context ctx) {
    Utils.cancelNotification(ctx, REPLY_NOTIFY_ID);
  }

  public static void onMessageDelivered(AVMessage avMsg, Set<MsgListener> listeners) {
    Msg msg = Msg.fromAVMessage(avMsg);
    DBMsg.updateStatus(msg.getObjectId(), Msg.Status.SendReceived);
    String otherId = msg.getToPeerId();
    for (MsgListener listener : listeners) {
      if (listener.onMessageUpdate(otherId)) {
        break;
      }
    }
  }

  public static boolean isSessionPaused() {
    return MsgReceiver.isSessionPaused();
  }
}
