package com.avoscloud.chat.ui.activity;

import android.os.Bundle;
import com.avoscloud.chat.service.event.ConvChangeEvent;
import com.avoscloud.chat.service.event.FinishEvent;
import de.greenrobot.event.EventBus;

/**
 * Created by lzw on 15/3/5.
 */
public abstract class ConvEventBaseActivity extends BaseActivity {
  private EventBus eventBus;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    eventBus = EventBus.getDefault();
    eventBus.register(this);
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    eventBus.unregister(this);
  }

  abstract public void onEvent(ConvChangeEvent convChangeEvent);

  public void onEvent(FinishEvent finishEvent) {
    this.finish();
  }
}
