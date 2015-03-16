package com.avoscloud.chat.ui.activity;

import com.avos.avoscloud.im.v2.AVIMConversation;
import com.avoscloud.chat.service.CacheService;
import com.avoscloud.chat.service.event.ConvChangeEvent;

/**
 * Created by lzw on 14/12/20.
 */
public abstract class ConvBaseActivity extends ConvEventBaseActivity {

  public static AVIMConversation conv() {
    return CacheService.getCurConv();
  }


  @Override
  public void onEvent(ConvChangeEvent convChangeEvent) {
    if (conv() != null && conv().getConversationId().
        equals(convChangeEvent.getConv().getConversationId())) {
      onConvChanged(convChangeEvent.getConv());
    }
  }

  protected abstract void onConvChanged(AVIMConversation conv);
}
