package com.avoscloud.chat.ui.activity;

import com.avoscloud.chat.avobject.ChatGroup;
import com.avoscloud.chat.service.CacheService;

/**
 * Created by lzw on 14/12/20.
 */
public abstract class GroupBaseActivity extends BaseActivity {
  public static ChatGroup getChatGroup() {
    return CacheService.getCurrentChatGroup();
  }

  abstract void onGroupUpdate();
}
