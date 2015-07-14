package com.avoscloud.leanchatlib.controller;

import android.content.Context;
import com.avos.avoscloud.AVException;
import com.avos.avoscloud.im.v2.AVIMClient;
import com.avos.avoscloud.im.v2.AVIMClientEventHandler;
import com.avos.avoscloud.im.v2.AVIMConversation;
import com.avos.avoscloud.im.v2.AVIMConversationEventHandler;
import com.avos.avoscloud.im.v2.AVIMConversationQuery;
import com.avos.avoscloud.im.v2.AVIMMessage;
import com.avos.avoscloud.im.v2.AVIMMessageManager;
import com.avos.avoscloud.im.v2.AVIMTypedMessage;
import com.avos.avoscloud.im.v2.AVIMTypedMessageHandler;
import com.avos.avoscloud.im.v2.callback.AVIMClientCallback;
import com.avos.avoscloud.im.v2.callback.AVIMConversationCreatedCallback;
import com.avos.avoscloud.im.v2.callback.AVIMConversationQueryCallback;
import com.avos.avoscloud.im.v2.callback.AVIMMessagesQueryCallback;
import com.avoscloud.leanchatlib.activity.ChatActivity;
import com.avoscloud.leanchatlib.model.ConversationType;
import com.avoscloud.leanchatlib.model.MessageEvent;
import com.avoscloud.leanchatlib.model.Room;
import com.avoscloud.leanchatlib.utils.LogUtils;
import de.greenrobot.event.EventBus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

/**
 * 该类来负责处理接收消息、聊天服务连接状态管理、查找对话、获取最近对话列表最后一条消息
 * Created by lzw on 15/2/10.
 */
public class ChatManager extends AVIMClientEventHandler {
  private static final String KEY_UPDATED_AT = "updatedAt";
  private static ChatManager chatManager;
  private static Context context;

  /**
   * 默认的聊天连接状态监听器
   */
  private static ConnectionListener defaultConnectListener = new ConnectionListener() {
    @Override
    public void onConnectionChanged(boolean connect) {
    }
  };
  //用来判断是否弹出通知
  public static String currentChattingConvid;

  private ConnectionListener connectionListener = defaultConnectListener;
  private Map<String, AVIMConversation> cachedConversations = new ConcurrentHashMap<String, AVIMConversation>();
  private volatile AVIMClient imClient;
  private volatile String selfId;
  private volatile boolean connect = false;
  private MessageHandler messageHandler;
  private RoomsTable roomsTable;
  private EventBus eventBus = EventBus.getDefault();
  private ChatManagerAdapter chatManagerAdapter;
  private static boolean debugEnabled;
  private Map<String, AVIMTypedMessage> latestMessages = new ConcurrentHashMap<String, AVIMTypedMessage>();

  private ChatManager() {
  }

  /**
   * 获取 ChatManager 单例
   *
   * @return
   */
  public static synchronized ChatManager getInstance() {
    if (chatManager == null) {
      chatManager = new ChatManager();
    }
    return chatManager;
  }

  public static Context getContext() {
    return context;
  }

  public static boolean isDebugEnabled() {
    return debugEnabled;
  }

  /**
   * 设置是否打印 leanchatlib 的日志，发布应用的时候要关闭
   * 日志 TAG 为 leanchatlib，可以获得一些异常日志
   *
   * @param debugEnabled
   */
  public static void setDebugEnabled(boolean debugEnabled) {
    ChatManager.debugEnabled = debugEnabled;
  }

  /**
   * 请在应用一启动(Application onCreate)的时候就调用，因为 SDK 一启动，就会去连接聊天服务器
   * 如果没有调用此函数设置 messageHandler ，就可能丢失一些消息
   *
   * @param context
   */
  public void init(Context context) {
    this.context = context;
    messageHandler = new MessageHandler();
    AVIMMessageManager.registerMessageHandler(AVIMTypedMessage.class, messageHandler);
//    try {
//      AVIMMessageManager.registerAVIMMessageType(AVIMUserInfoMessage.class);
//    } catch (AVException e) {
//      e.printStackTrace();
//    }
    AVIMClient.setClientEventHandler(this);
    //签名
    //AVIMClient.setSignatureFactory(new SignatureFactory());
  }

  /**
   * 设置 AVIMConversationEventHandler，用来处理对话成员变更回调
   *
   * @param eventHandler
   */
  public void setConversationEventHandler(AVIMConversationEventHandler eventHandler) {
    AVIMMessageManager.setConversationEventHandler(eventHandler);
  }

  /**
   * 请在登录之后，进入 MainActivity 之前，调用此函数，因为此时可以拿到当前登录用户的 ID
   *
   * @param userId 应用用户系统当前用户的 userId
   */
  public void setupManagerWithUserId(String userId) {
    this.selfId = userId;
    roomsTable = RoomsTable.getInstanceByUserId(userId);
  }

  /**
   * 监听聊天服务连接状态 ，这里不用 SDK 的 AVIMClientHandler
   * 是因为 SDK 在 open 的时候没有回调 onConnectResume ，不方便统一处理
   *
   * @param connectionListener
   */
  public void setConnectionListener(ConnectionListener connectionListener) {
    this.connectionListener = connectionListener;
  }

  public String getSelfId() {
    return selfId;
  }

  public RoomsTable getRoomsTable() {
    return roomsTable;
  }

  /**
   * 连接聊天服务器，用 userId 登录，在进入MainActivity 前调用
   *
   * @param callback AVException 常发生于网络错误、签名错误
   */
  public void openClientWithUserId(final AVIMClientCallback callback) {
    if (this.selfId == null) {
      throw new IllegalStateException("please call setupManagerWithUserId() first");
    }
    imClient = AVIMClient.getInstance(this.selfId);
    imClient.open(new AVIMClientCallback() {
      @Override
      public void done(AVIMClient client, AVException e) {
        if (e != null) {
          setConnectAndNotify(false);
        } else {
          setConnectAndNotify(true);
        }
        if (callback != null) {
          callback.done(client, e);
        }
      }
    });
  }

  private void onMessageReceipt(AVIMTypedMessage message) {
    MessageEvent messageEvent = new MessageEvent(message, MessageEvent.Type.Receipt);
    eventBus.post(messageEvent);
  }

  private void onMessage(final AVIMTypedMessage message, final AVIMConversation conversation) {
    if (message == null || message.getMessageId() == null) {
      LogUtils.d("may be SDK Bug, message or message id is null");
      return;
    }
    if (!ConversationHelper.isValidConversation(conversation)) {
      LogUtils.d("receive msg from invalid conversation");
    }
    if (lookUpConversationById(conversation.getConversationId()) == null) {
      registerConversation(conversation);
    }
    roomsTable.insertRoom(message.getConversationId());
    roomsTable.increaseUnreadCount(message.getConversationId());
    putLatestMessage(message);
    MessageEvent messageEvent = new MessageEvent(message, MessageEvent.Type.Come);
    eventBus.post(messageEvent);
    if (selfId != null && (ChatActivity.getCurrentChattingConvid() == null
        || !ChatActivity.getCurrentChattingConvid().equals(message.getConversationId()))) {
      chatManagerAdapter.shouldShowNotification(context, selfId, conversation, message);
    }
  }

  /**
   * 用户注销的时候调用，close 之后消息不会推送过来，也不可以进行发消息等操作
   *
   * @param callback AVException 常见于网络错误
   */
  public void closeWithCallback(final AVIMClientCallback callback) {
    imClient.close(new AVIMClientCallback() {

      @Override
      public void done(AVIMClient client, AVException e) {
        if (e != null) {
          LogUtils.logThrowable(e);
        }
        if (callback != null) {
          callback.done(client, e);
        }
      }
    });
    imClient = null;
    selfId = null;
  }

  /**
   * 获取和 userId 的对话，先去服务器查之前两人有没创建过对话，没有的话，创建一个
   *
   * @param userId
   * @param callback
   */
  public void fetchConversationWithUserId(String userId, final AVIMConversationCreatedCallback callback) {
    final List<String> members = new ArrayList<>();
    members.add(userId);
    members.add(selfId);
    AVIMConversationQuery query = imClient.getQuery();
    query.withMembers(members);
    query.whereEqualTo(ConversationType.ATTR_TYPE_KEY, ConversationType.Single.getValue());
    query.orderByDescending(KEY_UPDATED_AT);
    query.limit(1);
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

  /**
   * 获取 AVIMConversationQuery，用来查询对话
   *
   * @return
   */
  public AVIMConversationQuery getConversationQuery() {
    return imClient.getQuery();
  }

  /**
   * 创建对话，为了不暴露 AVIMClient，这里封装一下
   *
   * @param members    成员，需要包含自己
   * @param attributes 对话的附加属性
   * @param callback   AVException 聊天服务断开时抛出
   */
  public void createConversation(List<String> members, Map<String, Object> attributes,
                                 AVIMConversationCreatedCallback callback) {
    imClient.createConversation(members, attributes, callback);
  }

  @Override
  public void onConnectionPaused(AVIMClient client) {
    setConnectAndNotify(false);
  }

  @Override
  public void onConnectionResume(AVIMClient client) {
    setConnectAndNotify(true);
  }

  private void setConnectAndNotify(boolean connect) {
    this.connect = connect;
    connectionListener.onConnectionChanged(connect);
  }

  /**
   * 是否连上聊天服务
   *
   * @return
   */
  public boolean isConnect() {
    return connect;
  }

  /**
   * 在进入 ChatActivity 之前需要先注册一下该 Conversation
   *
   * @param conversation
   */
  public void registerConversation(AVIMConversation conversation) {
    cachedConversations.put(conversation.getConversationId(), conversation);
  }

  /**
   * 用在 ChatActivity 中，因为 AVIMConversation 还没支持序列化
   *
   * @param conversationId
   * @return
   */
  public AVIMConversation lookUpConversationById(String conversationId) {
    return cachedConversations.get(conversationId);
  }

  public ChatManagerAdapter getChatManagerAdapter() {
    return chatManagerAdapter;
  }

  public void setChatManagerAdapter(ChatManagerAdapter chatManagerAdapter) {
    this.chatManagerAdapter = chatManagerAdapter;
  }

  //ChatUser
  public List<Room> findRecentRooms() {
    return ChatManager.getInstance().getRoomsTable().selectRooms();
  }

  public interface ConnectionListener {
    void onConnectionChanged(boolean connect);
  }

  private static class MessageHandler extends AVIMTypedMessageHandler<AVIMTypedMessage> {

    @Override
    public void onMessage(AVIMTypedMessage message, AVIMConversation conversation,
                          AVIMClient client) {
      if (client.getClientId().equals(chatManager.getSelfId())) {
        chatManager.onMessage(message, conversation);
      } else {
        // 收到其它的client的消息，可能是上一次别的client登录未正确关闭，这里关边掉。
        client.close(null);
      }
    }

    @Override
    public void onMessageReceipt(AVIMTypedMessage message, AVIMConversation conversation,
                                 AVIMClient client) {
      if (client.getClientId().equals(chatManager.getSelfId())) {
        chatManager.onMessageReceipt(message);
      } else {
        client.close(null);
      }
    }
  }

  /**
   * msgId 、time 共同使用，防止某毫秒时刻有重复消息
   */
  public void queryMessages(AVIMConversation conversation, final String msgId, long time, final int limit,
                            final AVIMTypedMessagesArrayCallback callback) {
    conversation.queryMessages(msgId, time, limit, new AVIMMessagesQueryCallback() {
      @Override
      public void done(List<AVIMMessage> imMessages, AVException e) {
        if (e != null) {
          callback.done(Collections.EMPTY_LIST, e);
        } else {
          List<AVIMTypedMessage> resultMessages = new ArrayList<>(limit);
          for (AVIMMessage msg : imMessages) {
            if (msg instanceof AVIMTypedMessage) {
              resultMessages.add((AVIMTypedMessage) msg);
            } else {
              LogUtils.i("unexpected message " + msg.getContent());
            }
          }
          callback.done(resultMessages, null);
        }
      }
    });
  }

  public void putLatestMessage(AVIMTypedMessage message) {
    latestMessages.put(message.getConversationId(), message);
  }

  private AVIMTypedMessage getLatestMessage(String conversationId) {
    return latestMessages.get(conversationId);
  }


  /**
   * 查找对话的最后一条消息，如果已查找过，则立即返回
   * @param conversationId
   * @return 当向服务器查找失败时或无历史消息时，返回 null
   */

  public synchronized AVIMTypedMessage getOrQueryLatestMessage(String conversationId) {
    AVIMTypedMessage message = getLatestMessage(conversationId);
    if (message != null) {
      return message;
    }
    final CountDownLatch latch = new CountDownLatch(1);
    final List<AVIMTypedMessage> foundMessages = new ArrayList<>(1);
    queryMessages(imClient.getConversation(conversationId), null, System.currentTimeMillis(), 1, new AVIMTypedMessagesArrayCallback() {
      @Override
      public void done(List<AVIMTypedMessage> typedMessages, AVException e) {
        if (e == null && typedMessages != null) {
          foundMessages.addAll(typedMessages);
        }
        latch.countDown();
      }
    });
    try {
      latch.await();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    if (foundMessages.size() > 0) {
      putLatestMessage(foundMessages.get(0));
      return foundMessages.get(0);
    } else {
      return null;
    }
  }

}
