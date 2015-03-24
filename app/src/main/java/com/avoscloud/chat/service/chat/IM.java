package com.avoscloud.chat.service.chat;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import com.alibaba.fastjson.JSONException;
import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.im.v2.*;
import com.avos.avoscloud.im.v2.callback.AVIMClientCallback;
import com.avoscloud.chat.base.App;
import com.avoscloud.chat.db.MsgsTable;
import com.avoscloud.chat.db.RoomsTable;
import com.avoscloud.chat.service.CacheService;
import com.avoscloud.chat.service.PreferenceMap;
import com.avoscloud.chat.service.event.MsgEvent;
import com.avoscloud.chat.ui.activity.ChatActivity;
import com.avoscloud.chat.util.Logger;
import com.avoscloud.chat.util.NetAsyncTask;
import com.avoscloud.chat.util.Utils;
import de.greenrobot.event.EventBus;

import java.util.Random;

/**
 * Created by lzw on 15/2/10.
 */
public class IM extends AVIMClientEventHandler {
  private static final long NOTIFY_PERIOD = 1000;
  private static final int REPLY_NOTIFY_ID = 1;
  private static IM im;
  private static long lastNotifyTime = 0;

  private static ConnectionListener defaultConnectListener = new ConnectionListener() {
    @Override
    public void onConnectionChanged(boolean connect) {
      Logger.d("default connect listener");
    }
  };

  private ConnectionListener connectionListener = defaultConnectListener;
  private AVIMClient imClient;
  private String selfId;
  private boolean connect = false;
  private MsgHandler msgHandler;
  private MsgsTable msgsTable;
  private RoomsTable roomsTable;
  private EventBus eventBus = EventBus.getDefault();

  public IM() {
  }

  public synchronized static IM getInstance() {
    if (im == null) {
      im = new IM();
    }
    return im;
  }

  public static void notifyMsg(Context context, AVIMConversation conv, AVIMTypedMessage msg) throws JSONException {
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
    CharSequence notifyContent = MsgUtils.outlineOfMsg(msg);
    CharSequence username = "username";
    AVUser from = CacheService.lookupUser(msg.getFrom());
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
    PreferenceMap preferenceMap = PreferenceMap.getCurUserPrefDao(context);
    if (preferenceMap.isVoiceNotify()) {
      notification.defaults |= Notification.DEFAULT_SOUND;
    }
    if (preferenceMap.isVibrateNotify()) {
      notification.defaults |= Notification.DEFAULT_VIBRATE;
    }
    man.notify(REPLY_NOTIFY_ID, notification);
  }

  public void init() {
    msgHandler = new MsgHandler();
    msgsTable = MsgsTable.getInstance();
    roomsTable = RoomsTable.getInstance();
    AVIMMessageManager.registerMessageHandler(AVIMTypedMessage.class, msgHandler);
    AVIMMessageManager.setConversationEventHandler(ConvManager.getConvHandler());
    AVIMClient.setClientEventHandler(this);
    //签名
    //AVIMClient.setSignatureFactory(new SignatureFactory());
  }

  public void setConnectionListener(ConnectionListener connectionListener) {
    this.connectionListener = connectionListener;
  }

  public void onMessage(final AVIMConversation conv, final AVIMTypedMessage msg) {
    Logger.d("receive message=" + msg.getContent());
    if (msg.getMessageId() == null) {
      throw new NullPointerException("message id is null");
    }
    CacheService.registerConvIfNone(conv);
    msgsTable.insertMsg(msg);
    roomsTable.insertRoom(msg.getConversationId());
    roomsTable.increaseUnreadCount(msg.getConversationId());
    MsgEvent msgEvent = new MsgEvent(msg);
    eventBus.post(msgEvent);
    new NetAsyncTask(App.ctx, false) {
      @Override
      protected void doInBack() throws Exception {
        CacheService.cacheUserIfNone(msg.getFrom());
      }

      @Override
      protected void onPost(Exception e) {
        boolean chatting = ChatActivity.instance != null && ChatActivity.instance.isVisible()
            && CacheService.isCurConvid(msg.getConversationId());
        if (!chatting && AVUser.getCurrentUser() != null) {
          PreferenceMap preferenceMap = PreferenceMap.getCurUserPrefDao(App.ctx);
          if (preferenceMap.isNotifyWhenNews()) {
            notifyMsg(App.ctx, conv, msg);
          }
        }
      }
    }.execute();
  }

  public void cancelNotification() {
    Utils.cancelNotification(App.ctx, REPLY_NOTIFY_ID);
  }

  public void onMessageDelivered(AVIMTypedMessage msg) {
    if (msg.getMessageId() == null) {
      throw new NullPointerException("message id is null");
    }
    msgsTable.updateStatus(msg.getMessageId(), msg.getMessageStatus());
    MsgEvent msgEvent = new MsgEvent(msg);
    eventBus.post(msgEvent);
  }

  public AVIMClient getImClient() {
    return imClient;
  }

  public String getSelfId() {
    return selfId;
  }

  public void open(String selfId) {
    imClient = AVIMClient.getInstance(selfId);
    this.selfId = selfId;
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

  public void close() {
    imClient.close(new AVIMClientCallback() {

      @Override
      public void done(AVIMClient client, AVException e) {
        if (Utils.filterException(e)) {

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
    public void onMessage(AVIMTypedMessage message, AVIMConversation conversation, AVIMClient client) {
      getInstance().onMessage(conversation, message);
    }

    @Override
    public void onMessageReceipt(AVIMTypedMessage message, AVIMConversation conversation, AVIMClient client) {
      getInstance().onMessageDelivered(message);
    }
  }
}
