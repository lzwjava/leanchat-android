package com.avoscloud.chat.ui.chat;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import com.avos.avoscloud.AVException;
import com.avos.avoscloud.im.v2.AVIMConversation;
import com.avos.avoscloud.im.v2.callback.AVIMConversationCallback;
import com.avos.avoscloud.im.v2.callback.AVIMConversationCreatedCallback;
import com.avoscloud.chat.R;
import com.avoscloud.chat.entity.AVIMUserInfoMessage;
import com.avoscloud.leanchatlib.activity.ChatActivity;
import com.avoscloud.leanchatlib.controller.ChatManager;
import com.avoscloud.leanchatlib.controller.ConversationHelper;
import com.avoscloud.leanchatlib.utils.Logger;
import com.avoscloud.chat.service.CacheService;
import com.avoscloud.chat.service.ConversationChangeEvent;
import com.avoscloud.chat.service.event.FinishEvent;
import com.avoscloud.chat.ui.conversation.ConversationDetailActivity;
import com.avoscloud.chat.util.Utils;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by lzw on 15/4/24.
 */
public class ChatRoomActivity extends ChatActivity {

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
  }

  @Override
  protected void onResume() {
    CacheService.setCurConv(conversation);
    super.onResume();
  }

  @Override
  protected void onDestroy() {
    CacheService.setCurConv(null);
    super.onDestroy();
  }

  public static void chatByConversation(Context from, AVIMConversation conv) {
    CacheService.registerConv(conv);
    ChatManager.getInstance().registerConversation(conv);
    Intent intent = new Intent(from, ChatRoomActivity.class);
    intent.putExtra(CONVID, conv.getConversationId());
    from.startActivity(intent);
  }

  public static void chatByUserId(final Activity from, String userId) {
    final ProgressDialog dialog = Utils.showSpinnerDialog(from);
    ChatManager.getInstance().fetchConversationWithUserId(userId, new AVIMConversationCreatedCallback() {
      @Override
      public void done(AVIMConversation conversation, AVException e) {
        dialog.dismiss();
        if (Utils.filterException(e)) {
          chatByConversation(from, conversation);
        }
      }
    });
  }

  private void testSendCustomMessage() {
    AVIMUserInfoMessage userInfoMessage = new AVIMUserInfoMessage();
    Map<String, Object> map = new HashMap<>();
    map.put("nickname", "lzwjava");
    userInfoMessage.setAttrs(map);
    conversation.sendMessage(userInfoMessage, new AVIMConversationCallback() {
      @Override
      public void done(AVException e) {
        if (e != null) {
          Logger.d(e.getMessage());
        }
      }
    });
  }

  public void onEvent(ConversationChangeEvent conversationChangeEvent) {
    if (conversation != null && conversation.getConversationId().
        equals(conversationChangeEvent.getConv().getConversationId())) {
      this.conversation = conversationChangeEvent.getConv();
      ActionBar actionBar = getActionBar();
      actionBar.setTitle(ConversationHelper.titleOfConv(this.conversation));
    }
  }

  public void onEvent(FinishEvent finishEvent) {
    this.finish();
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.chat_ativity_menu, menu);
    return super.onCreateOptionsMenu(menu);
  }

  @Override
  public boolean onMenuItemSelected(int featureId, MenuItem item) {
    int menuId = item.getItemId();
    if (menuId == R.id.people) {
      Utils.goActivity(ctx, ConversationDetailActivity.class);
    }
    return super.onMenuItemSelected(featureId, item);
  }
}
