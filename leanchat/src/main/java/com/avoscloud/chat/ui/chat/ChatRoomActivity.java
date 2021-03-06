package com.avoscloud.chat.ui.chat;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import com.avos.avoscloud.AVException;
import com.avos.avoscloud.im.v2.AVIMConversation;
import com.avos.avoscloud.im.v2.AVIMException;
import com.avos.avoscloud.im.v2.callback.AVIMConversationCallback;
import com.avos.avoscloud.im.v2.callback.AVIMConversationCreatedCallback;
import com.avos.avoscloud.im.v2.messages.AVIMImageMessage;
import com.avos.avoscloud.im.v2.messages.AVIMLocationMessage;
import com.avoscloud.chat.R;
import com.avoscloud.chat.entity.AVIMUserInfoMessage;
import com.avoscloud.chat.service.CacheService;
import com.avoscloud.chat.service.ChatManagerAdapterImpl;
import com.avoscloud.chat.service.ConversationChangeEvent;
import com.avoscloud.chat.service.event.FinishEvent;
import com.avoscloud.chat.ui.conversation.ConversationDetailActivity;
import com.avoscloud.chat.util.Logger;
import com.avoscloud.chat.util.Utils;
import com.avoscloud.leanchatlib.activity.ChatActivity;
import com.avoscloud.leanchatlib.activity.ChatActivityEventListener;
import com.avoscloud.leanchatlib.controller.ChatManager;
import com.avoscloud.leanchatlib.controller.ConversationHelper;
import com.avoscloud.leanchatlib.controller.MessageHelper;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by lzw on 15/4/24.
 */
public class ChatRoomActivity extends ChatActivity implements ChatActivityEventListener {
  public static final int LOCATION_REQUEST = 100;

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
      public void done(AVIMConversation conversation, AVIMException e) {
        dialog.dismiss();
        if (Utils.filterException(e)) {
          chatByConversation(from, conversation);
        }
      }
    });
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    initLocation();
  }

  private void initLocation() {
    addLocationBtn.setVisibility(View.VISIBLE);
  }

  @Override
  protected void onResume() {
    CacheService.setCurrentConversation(conversation);
    ChatManagerAdapterImpl chatManagerAdapter = (ChatManagerAdapterImpl) ChatManager.getInstance().getChatManagerAdapter();
    chatManagerAdapter.cancelNotification();
    super.onResume();
  }

  @Override
  protected void onDestroy() {
    CacheService.setCurrentConversation(null);
    super.onDestroy();
  }

  private void testSendCustomMessage() {
    AVIMUserInfoMessage userInfoMessage = new AVIMUserInfoMessage();
    Map<String, Object> map = new HashMap<>();
    map.put("nickname", "lzwjava");
    userInfoMessage.setAttrs(map);
    conversation.sendMessage(userInfoMessage, new AVIMConversationCallback() {
      @Override
      public void done(AVIMException e) {
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
      actionBar.setTitle(ConversationHelper.titleOfConversation(this.conversation));
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
      Intent intent = new Intent(this, ConversationDetailActivity.class);
      startActivity(intent);
    }
    return super.onMenuItemSelected(featureId, item);
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
    super.onActivityResult(requestCode, resultCode, intent);
    if (resultCode == RESULT_OK) {
      switch (requestCode) {
        case LOCATION_REQUEST:
          final double latitude = intent.getDoubleExtra(LocationActivity.LATITUDE, 0);
          final double longitude = intent.getDoubleExtra(LocationActivity.LONGITUDE, 0);
          final String address = intent.getStringExtra(LocationActivity.ADDRESS);
          if (!TextUtils.isEmpty(address)) {
            messageAgent.sendLocation(latitude, longitude, address);
          } else {
            toast(R.string.chat_cannotGetYourAddressInfo);
          }
          hideBottomLayout();
          break;
      }
    }
  }

  @Override
  public void onAddLocationButtonClicked(View v) {
    LocationActivity.startToSelectLocationForResult(this, LOCATION_REQUEST);
  }

  @Override
  public void onLocationMessageViewClicked(AVIMLocationMessage locationMessage) {
    LocationActivity.startToSeeLocationDetail(this, locationMessage.getLocation().getLatitude(),
        locationMessage.getLocation().getLongitude());
  }


  @Override
  public void onImageMessageViewClicked(AVIMImageMessage imageMessage, String localImagePath) {
    ImageBrowserActivity.go(ChatRoomActivity.this, MessageHelper.getFilePath(imageMessage),
        imageMessage.getFileUrl());
  }
}
