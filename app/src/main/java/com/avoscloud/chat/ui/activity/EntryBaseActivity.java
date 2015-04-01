package com.avoscloud.chat.ui.activity;

import android.os.Bundle;
import com.avoscloud.chat.service.event.LoginFinishEvent;
import de.greenrobot.event.EventBus;

/**
 * Created by lzw on 14/11/20.
 */
public class EntryBaseActivity extends BaseActivity {
  private EventBus eventBus = EventBus.getDefault();

  public void onEvent(LoginFinishEvent loginFinishEvent) {
    finish();
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    eventBus.register(this);
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    eventBus.unregister(this);
  }
}
