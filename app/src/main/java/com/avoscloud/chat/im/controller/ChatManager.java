package com.avoscloud.chat.im.controller;

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
import com.avoscloud.chat.im.activity.ChatActivity;
import com.avoscloud.chat.im.db.MsgsTable;
import com.avoscloud.chat.im.db.RoomsTable;
import com.avoscloud.chat.im.model.ChatUser;
import com.avoscloud.chat.im.model.ConversationType;
import com.avoscloud.chat.im.model.MessageEvent;
import com.avoscloud.chat.im.model.Room;
import com.avoscloud.chat.im.utils.Logger;
import com.avoscloud.chat.im.utils.NetAsyncTask;
import de.greenrobot.event.EventBus;

import java.util.*;

/**
 * Created by lzw on 15/2/10.
 */
public class ChatManager extends AVIMClientEventHandler {
  private static final long NOTIFY_PERIOD = 1000;
  private static final int REPLY_NOTIFY_ID = 1;
  public static final String KEY_UPDATED_AT = "updatedAt";
  private static ChatManager chatManager;
  private static long lastNotifyTime = 0;
  private static Context context;
  private Map<String, AVIMConversation> cachedConversations = new HashMap<>();

  private static ConnectionListener defaultConnectListener = new ConnectionListener() {
    @Override
    public void onConnectionChanged(boolean connect) {
      Logger.d("default connect listener");
    }
  };

  private ConnectionListener connectionListener = defaultConnectListener;
  private static boolean setupDatabase = false;
  private AVIMClient imClient;
  private String selfId;
  private boolean connect = false;
  private MsgHandler msgHandler;
  private MsgsTable msgsTable;
  private RoomsTable roomsTable;
  private EventBus eventBus = EventBus.getDefault();
  private ChatUserFactory chatUserFactory;

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
    ChatUser from = getChatUserFactory().getChatUserById(msg.getFrom());
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
    getChatUserFactory().configureNotification(notification);
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

  public void openClientWithSelfId(String selfId) {
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
        getChatUserFactory().cacheUserByIdsInBackground(Arrays.asList(message.getFrom()));
      }

      @Override
      protected void onPost(Exception exception) {
        if (ChatActivity.getCurrentChattingConvid() != null && !ChatActivity.getCurrentChattingConvid().equals(message
            .getConversationId()) && selfId != null) {
          if (getChatUserFactory().showNotificationWhenNewMessageCome(selfId)) {
            showMessageNotification(getContext(), conversation, message);
          }
        }
      }
    }.execute();
  }

  public void close() {
    imClient.close(new AVIMClientCallback() {

      @Override
      public void done(AVIMClient client, AVException e) {
        if (e != null) {
          Logger.d(e.getMessage());
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

  //cache
  public void registerConversation(AVIMConversation conversation) {
    cachedConversations.put(conversation.getConversationId(), conversation);
  }

  public AVIMConversation lookUpConversationById(String conversationId) {
    return cachedConversations.get(conversationId);
  }

  //ChatUser

  public ChatUserFactory getChatUserFactory() {
    return chatUserFactory;
  }

  public void setChatUserFactory(ChatUserFactory chatUserFactory) {
    this.chatUserFactory = chatUserFactory;
  }

  public List<Room> findRecentRooms() {
    RoomsTable roomsTable = RoomsTable.getCurrentUserInstance();
    return roomsTable.selectRooms();
  }
}
