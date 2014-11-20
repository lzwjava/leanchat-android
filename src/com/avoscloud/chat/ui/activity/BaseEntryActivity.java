package com.avoscloud.chat.ui.activity;

import android.os.Bundle;
import com.avoscloud.chat.service.receiver.FinishReceiver;

/**
 * Created by lzw on 14/11/20.
 */
public class BaseEntryActivity extends BaseActivity {
  FinishReceiver finishReceiver;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    finishReceiver = FinishReceiver.register(this);
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    unregisterReceiver(finishReceiver);
  }
}
