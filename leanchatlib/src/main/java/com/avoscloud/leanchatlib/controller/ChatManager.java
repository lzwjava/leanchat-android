package com.avoscloud.leanchatlib.controller;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import com.avos.avoscloud.AVException;
import com.avos.avoscloud.im.v2.*;
import com.avos.avoscloud.im.v2.callback.AVIMClientCallback;
import com.avos.avoscloud.im.v2.callback.AVIMConversationCreatedCallback;
import com.avos.avoscloud.im.v2.callback.AVIMConversationQueryCallback;
import com.avoscloud.leanchatlib.activity.ChatActivity;
import com.avoscloud.leanchatlib.db.MsgsTable;
import com.avoscloud.leanchatlib.db.RoomsTable;
import com.avoscloud.leanchatlib.model.UserInfo;
import com.avoscloud.leanchatlib.model.ConversationType;
import com.avoscloud.leanchatlib.model.MessageEvent;
import com.avoscloud.leanchatlib.model.Room;
import com.avoscloud.leanchatlib.utils.Logger;
import com.avoscloud.leanchatlib.utils.NetAsyncTask;
import de.greenrobot.event.EventBus;

import java.util.*;

/**
 * Created by lzw on 15/2/10.
 */
public class ChatManager extends AVIMClientEventHandler {
  public static final String KEY_UPDATED_AT = "updatedAt";
  private static final long NOTIFY_PERIOD = 1000;
  private static final int REPLY_NOTIFY_ID = 1;
  private static ChatManager chatManager;
  private static long lastNotifyTime = 0;
  private static Context context;
  private static ConnectionListener defaultConnectListener = new ConnectionListener() {
    @Override
    public void onConnectionChanged(boolean connect) {
      Logger.d("default connect listener");
    }
  };
  private ConnectionListener connectionListener = defaultConnectListener;
  private static boolean setupDatabase = false;
  private Map<String, AVIMConversation> cachedConversations = new HashMap<>();
  private AVIMClient imClient;
  private String selfId;
  private boolean connect = false;
  private MsgHandler msgHandler;
  private MsgsTable msgsTable;
  private RoomsTable roomsTable;
  private EventBus eventBus = EventBus.getDefault();
  private UserInfoFactory userInfoFactory;

  private ChatManager() {
  }

  public static synchronized ChatManager getInstance() {
    if (chatManager == null) {
      chatManager = new ChatManager();
    }
    return chatManager;
  }

  public static Context getContext() {
    return context;
  }

  // fetchConversation
  public void fetchConversationWithUserId(String userId, final AVIMConversationCreatedCallback callback) {
    final List<String> members = new ArrayList<>();
    members.add(userId);
    members.add(selfId);
    AVIMConversationQuery query = imClient.getQuery();
    query.withMembers(members);
    query.whereEqualTo(ConversationType.ATTR_TYPE_KEY, ConversationType.Single.getValue());
    query.orderByDescending(KEY_UPDATED_AT);
    query.findInBackground(new AVIMConversationQueryCallback() {
      @Override
      public void done(List<AVIMConversation> conversations, AVException e) {
        if (e != null) {
          callback.done(null, e);
        } else {
          if (conversations.size() > 0) {
            callback.done(conversations.get(0), null);
          } else {
            Map<String, Object> attrs = new HashMap<>();
            attrs.put(ConversationType.TYPE_KEY, ConversationType.Single.getValue());
            imClient.createConversation(members, attrs, callback);
          }
        }
      }
    });
  }

  public void showMessageNotification(Context context, AVIMConversation conv, AVIMTypedMessage msg) {
    if (System.currentTimeMillis() - lastNotifyTime < NOTIFY_PERIOD) {
      return;
    } else {
      lastNotifyTime = System.currentTimeMillis();
    }
    int icon = context.getApplicationInfo().icon;
    Intent intent = new Intent(context, ChatActivity.class);
    intent.putExtra(ChatActivity.CONVID, conv.getConversationId());

    //why Random().nextInt()
    //http://stackoverflow.com/questions/13838313/android-onnewintent-always-receives-same-intent
    PendingIntent pend = PendingIntent.getActivity(context, new Random().nextInt(),
        intent, 0);
    Notification.Builder builder = new Notification.Builder(context);
    CharSequence notifyContent = MessageHelper.outlineOfMsg(msg);
    CharSequence username = "username";
    UserInfo from = getUserInfoFactory().getUserInfoById(msg.getFrom());
    if (from != null) {
      username = from.getUsername();
    }
    builder.setContentIntent(pend)
        .setSmallIcon(icon)
        .setWhen(System.currentTimeMillis())
        .setTicker(username + "\n" + notifyContent)
        .setContentTitle(username)
        .setContentText(notifyContent)
        .setAutoCancel(true);
    NotificationManager man = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    Notification notification = builder.getNotification();
    getUserInfoFactory().configureNotification(notification);
    man.notify(REPLY_NOTIFY_ID, notification);
  }

  public void init(Context context) {
    this.context = context;
    msgHandler = new MsgHandler();
    AVIMMessageManager.registerMessageHandler(AVIMTypedMessage.class, msgHandler);
//    try {
//      AVIMMessageManager.registerAVIMMessageType(AVIMUserInfoMessage.class);
//    } catch (AVException e) {
//      e.printStackTrace();
//    }

    AVIMClient.setClientEventHandler(this);
    //签名
    //AVIMClient.setSignatureFactory(new SignatureFactory());
  }

  public void setConversationEventHandler(AVIMConversationEventHandler eventHandler) {
    AVIMMessageManager.setConversationEventHandler(eventHandler);
  }

  public void setupDatabaseWithSelfId(String selfId) {
    this.selfId = selfId;
    if (setupDatabase) {
      return;
    }
    setupDatabase = true;
    msgsTable = MsgsTable.getCurrentUserInstance();
    roomsTable = RoomsTable.getCurrentUserInstance();
  }

  public void setConnectionListener(ConnectionListener connectionListener) {
    this.connectionListener = connectionListener;
  }

  public void cancelNotification() {
    NotificationManager nMgr = (NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE);
    nMgr.cancel(REPLY_NOTIFY_ID);
  }

  public AVIMClient getImClient() {
    return imClient;
  }

  public String getSelfId() {
    return selfId;
  }

  public void openClientWithSelfId(String selfId, final AVIMClientCallback callback) {
    if (this.selfId == null) {
      throw new IllegalStateException("please call setupDatabaseWithSelfId() first");
    }
    if (!this.selfId.equals(selfId)) {
      throw new IllegalStateException("setupDatabaseWithSelfId and openClient's selfId should be equal");
    }
    imClient = AVIMClient.getInstance(selfId);
    imClient.open(new AVIMClientCallback() {
      @Override
      public void done(AVIMClient client, AVException e) {
        if (e != null) {
          connect = false;
          connectionListener.onConnectionChanged(connect);
        } else {
          connect = true;
          connectionListener.onConnectionChanged(connect);
        }
        if (callback != null) {
          callback.done(client, e);
        }
      }
    });
  }

  private void onMessageReceipt(AVIMTypedMessage message, AVIMConversation conv) {
    if (message.getMessageId() == null) {
      throw new NullPointerException("message id is null");
    }
    msgsTable.updateStatus(message.getMessageId(), message.getMessageStatus());
    MessageEvent messageEvent = new MessageEvent(message);
    eventBus.post(messageEvent);
  }

  private void onMessage(final AVIMTypedMessage message, final AVIMConversation conversation) {
    Logger.d("receive message=" + message.getContent());
    if (message.getMessageId() == null) {
      throw new NullPointerException("message id is null");
    }
    if (!ConversationHelper.isValidConv(conversation)) {
      throw new IllegalStateException("receive msg from invalid conversation");
    }
    if (lookUpConversationById(conversation.getConversationId()) == null) {
      registerConversation(conversation);
    }
    msgsTable.insertMsg(message);
    roomsTable.insertRoom(message.getConversationId());
    roomsTable.increaseUnreadCount(message.getConversationId());
    MessageEvent messageEvent = new MessageEvent(message);
    eventBus.post(messageEvent);
    new NetAsyncTask(getContext(), false) {
      @Override
      protected void doInBack() throws Exception {
        getUserInfoFactory().cacheUserInfoByIdsInBackground(Arrays.asList(message.getFrom()));
      }

      @Override
      protected void onPost(Exception exception) {
        if (selfId != null && ChatActivity.getCurrentChattingConvid() == null || !ChatActivity.getCurrentChattingConvid().equals(message
            .getConversationId())) {
          if (getUserInfoFactory().showNotificationWhenNewMessageCome(selfId)) {
            showMessageNotification(getContext(), conversation, message);
          }
        }
      }
    }.execute();
  }

  public void closeWithCallback(final AVIMClientCallback callback) {
    imClient.close(new AVIMClientCallback() {

      @Override
      public void done(AVIMClient client, AVException e) {
        if (e != null) {
          Logger.d(e.getMessage());
        }
        if (callback != null) {
          callback.done(client, e);
        }
      }
    });
    imClient = null;
    selfId = null;
  }

  public AVIMConversationQuery getQuery() {
    return imClient.getQuery();
  }

  @Override
  public void onConnectionPaused(AVIMClient client) {
    Logger.d("connect paused");
    connect = false;
    connectionListener.onConnectionChanged(connect);
  }

  @Override
  public void onConnectionResume(AVIMClient client) {
    Logger.d("connect resume");
    connect = true;
    connectionListener.onConnectionChanged(connect);
  }

  public boolean isConnect() {
    return connect;
  }

  //cache
  public void registerConversation(AVIMConversation conversation) {
    cachedConversations.put(conversation.getConversationId(), conversation);
  }

  public AVIMConversation lookUpConversationById(String conversationId) {
    return cachedConversations.get(conversationId);
  }

  public UserInfoFactory getUserInfoFactory() {
    return userInfoFactory;
  }

  public void setUserInfoFactory(UserInfoFactory userInfoFactory) {
    this.userInfoFactory = userInfoFactory;
  }

  //ChatUser

  public List<Room> findRecentRooms() {
    RoomsTable roomsTable = RoomsTable.getCurrentUserInstance();
    return roomsTable.selectRooms();
  }

  public interface ConnectionListener {
    void onConnectionChanged(boolean connect);
  }

  private static class MsgHandler extends AVIMTypedMessageHandler<AVIMTypedMessage> {

    @Override
    public void onMessage(AVIMTypedMessage message, AVIMConversation conversation,
                          AVIMClient client) {
      chatManager.onMessage(message, conversation);
    }

    @Override
    public void onMessageReceipt(AVIMTypedMessage message, AVIMConversation conversation,
                                 AVIMClient client) {
      chatManager.onMessageReceipt(message, conversation);
    }
  }
}
