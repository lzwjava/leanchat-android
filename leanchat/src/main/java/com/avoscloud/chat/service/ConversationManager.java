package com.avoscloud.chat.service;

import android.graphics.Bitmap;
import com.avos.avoscloud.AVException;
import com.avos.avoscloud.im.v2.AVIMClient;
import com.avos.avoscloud.im.v2.AVIMConversation;
import com.avos.avoscloud.im.v2.AVIMConversationEventHandler;
import com.avos.avoscloud.im.v2.AVIMConversationQuery;
import com.avos.avoscloud.im.v2.AVIMMessage;
import com.avos.avoscloud.im.v2.AVIMTypedMessage;
import com.avos.avoscloud.im.v2.callback.AVIMConversationCallback;
import com.avos.avoscloud.im.v2.callback.AVIMConversationCreatedCallback;
import com.avos.avoscloud.im.v2.callback.AVIMConversationQueryCallback;
import com.avos.avoscloud.im.v2.callback.AVIMMessagesQueryCallback;
import com.avoscloud.chat.R;
import com.avoscloud.chat.base.App;
import com.avoscloud.chat.base.Constant;
import com.avoscloud.chat.util.Logger;
import com.avoscloud.chat.util.Utils;
import com.avoscloud.leanchatlib.controller.ChatManager;
import com.avoscloud.leanchatlib.controller.ConversationHelper;
import com.avoscloud.leanchatlib.controller.MessageAgent;
import com.avoscloud.leanchatlib.controller.MessageHelper;
import com.avoscloud.leanchatlib.model.ConversationType;
import com.avoscloud.leanchatlib.model.Room;
import de.greenrobot.event.EventBus;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

/**
 * Created by lzw on 15/2/11.
 */
public class ConversationManager {
  private static ConversationManager conversationManager;
  private static AVIMConversationEventHandler eventHandler = new AVIMConversationEventHandler() {
    @Override
    public void onMemberLeft(AVIMClient client, AVIMConversation conversation, List<String> members, String kickedBy) {
      Utils.toast(MessageHelper.nameByUserIds(members) + " left, kicked by " + MessageHelper.nameByUserId(kickedBy));
      CacheService.registerConv(conversation);
      getInstance().postConvChanged(conversation);
    }

    @Override
    public void onMemberJoined(AVIMClient client, AVIMConversation conversation, List<String> members, String invitedBy) {
      Utils.toast(MessageHelper.nameByUserIds(members) + " joined , invited by " + MessageHelper.nameByUserId(invitedBy));
      CacheService.registerConv(conversation);
      getInstance().postConvChanged(conversation);
    }

    @Override
    public void onKicked(AVIMClient client, AVIMConversation conversation, String kickedBy) {
      Utils.toast("you are kicked by " + MessageHelper.nameByUserId(kickedBy));
    }

    @Override
    public void onInvited(AVIMClient client, AVIMConversation conversation, String operator) {
      Utils.toast("you are invited by " + MessageHelper.nameByUserId(operator));
    }
  };

  public ConversationManager() {
  }

  public static synchronized ConversationManager getInstance() {
    if (conversationManager == null) {
      conversationManager = new ConversationManager();
    }
    return conversationManager;
  }

  public static AVIMConversationEventHandler getEventHandler() {
    return eventHandler;
  }

  public static AVIMTypedMessage getLastMessage(final AVIMConversation conversation) throws AVException, InterruptedException {
    final CountDownLatch latch = new CountDownLatch(1);
    final AVException[] es = new AVException[1];
    final List<AVIMTypedMessage> foundMessages = new ArrayList<>();
    conversation.queryMessages(1, new AVIMMessagesQueryCallback() {
      @Override
      public void done(List<AVIMMessage> messages, AVException e) {
        es[0] = e;
        if (e == null) {
          if (messages == null) {
            Logger.d("null conversationId=" + conversation.getConversationId());
          }
          for (AVIMMessage message : messages) {
            if (message instanceof AVIMTypedMessage) {
              foundMessages.add((AVIMTypedMessage) message);
            }
          }
        }
        latch.countDown();
      }
    });
    latch.await();
    if (es[0] != null) {
      throw es[0];
    }
    if (foundMessages.size() > 0) {
      return foundMessages.get(0);
    } else {
      return null;
    }
  }

  public List<Room> findAndCacheRooms() throws AVException, InterruptedException {
    List<Room> rooms = ChatManager.getInstance().findRecentRooms();
    List<String> convids = new ArrayList<>();
    for (Room room : rooms) {
      convids.add(room.getConversationId());
    }
    final CountDownLatch latch = new CountDownLatch(1);
    final AVException[] es = new AVException[1];
    CacheService.cacheConvs(convids, new AVIMConversationCallback() {
      @Override
      public void done(AVException e) {
        es[0] = e;
        latch.countDown();
      }
    });
    latch.await();
    if (es[0] != null) {
      throw es[0];
    }
    List<String> userIds = new ArrayList<>();
    for (Room room : rooms) {
      AVIMConversation conversation = CacheService.lookupConv(room.getConversationId());
      room.setConversation(conversation);
      room.setLastMessage(getLastMessage(conversation));
      if (ConversationHelper.typeOfConversation(conversation) == ConversationType.Single) {
        userIds.add(ConversationHelper.otherIdOfConversation(conversation));
      }
    }
    Collections.sort(rooms, new Comparator<Room>() {
      @Override
      public int compare(Room lhs, Room rhs) {
        if (lhs.getLastMessage() != null && rhs.getLastMessage() != null) {
          long value = lhs.getLastMessage().getTimestamp() - rhs.getLastMessage().getTimestamp();
          if (value > 0) {
            return -1;
          } else if (value < 0) {
            return 1;
          }
        }
        return 0;
      }
    });
    CacheService.cacheUsers(new ArrayList<>(userIds));
    return rooms;
  }

  public void updateName(final AVIMConversation conv, String newName, final AVIMConversationCallback callback) {
    conv.setName(newName);
    conv.updateInfoInBackground(new AVIMConversationCallback() {
      @Override
      public void done(AVException e) {
        if (e != null) {
          callback.done(e);
        } else {
          postConvChanged(conv);
          callback.done(null);
        }
      }
    });
  }

  public void postConvChanged(AVIMConversation conv) {
    if (CacheService.getCurConv() != null && CacheService.getCurConv().getConversationId().equals(conv.getConversationId())) {
      CacheService.setCurConv(conv);
    }
    ConversationChangeEvent conversationChangeEvent = new ConversationChangeEvent(conv);
    EventBus.getDefault().post(conversationChangeEvent);
  }

  public void findGroupConversationsIncludeMe(AVIMConversationQueryCallback callback) {
    AVIMConversationQuery q = ChatManager.getInstance().getQuery();
    q.containsMembers(Arrays.asList(ChatManager.getInstance().getSelfId()));
    q.whereEqualTo(ConversationType.ATTR_TYPE_KEY, ConversationType.Group.getValue());
    q.orderByDescending(Constant.UPDATED_AT);
    q.findInBackground(callback);
  }

  public void findConversationsByConversationIds(List<String> ids, AVIMConversationQueryCallback callback) {
    if (ids.size() > 0) {
      AVIMConversationQuery q = ChatManager.getInstance().getQuery();
      q.whereContainsIn(Constant.OBJECT_ID, ids);
      q.findInBackground(callback);
    } else {
      callback.done(new ArrayList<AVIMConversation>(), null);
    }
  }

  public void createGroupConversation(List<String> members, final AVIMConversationCreatedCallback callback) {
    Map<String, Object> map = new HashMap<String, Object>();
    map.put(ConversationType.TYPE_KEY, ConversationType.Group.getValue());
    final String name = MessageHelper.nameByUserIds(members);
    map.put(ConversationType.NAME_KEY, name);
    ChatManager.getInstance().getImClient().createConversation(members, map, callback);
  }

  public static Bitmap getConversationIcon(AVIMConversation conversation) {
    return ColoredBitmapProvider.getInstance().createColoredBitmapByHashString(conversation.getConversationId());
  }

  public void sendWelcomeMessage(String toUserId) {
    ChatManager.getInstance().fetchConversationWithUserId(toUserId,
        new AVIMConversationCreatedCallback() {
          @Override
          public void done(AVIMConversation avimConversation, AVException e) {
            if (e == null) {
              MessageAgent agent = new MessageAgent(avimConversation);
              agent.sendText(App.ctx.getString(R.string.message_when_agree_request));
            }
          }
        });
  }
}
