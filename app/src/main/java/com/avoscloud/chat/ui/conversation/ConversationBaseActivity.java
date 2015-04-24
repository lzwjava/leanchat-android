package com.avoscloud.chat.ui.conversation;

import com.avos.avoscloud.im.v2.AVIMConversation;
import com.avoscloud.chat.im.controller.ConversationChangeEvent;
import com.avoscloud.chat.service.CacheService;

/**
 * Created by lzw on 14/12/20.
 */
public abstract class ConversationBaseActivity extends ConversationEventBaseActivity {

  public static AVIMConversation conv() {
    return CacheService.getCurConv();
  }


  @Override
  public void onEvent(ConversationChangeEvent conversationChangeEvent) {
    if (conv() != null && conv().getConversationId().
        equals(conversationChangeEvent.getConv().getConversationId())) {
      onConvChanged(conversationChangeEvent.getConv());
    }
  }

  protected abstract void onConvChanged(AVIMConversation conv);
}
