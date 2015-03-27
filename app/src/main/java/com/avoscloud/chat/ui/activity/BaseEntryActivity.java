package com.avoscloud.chat.ui.activity;

import android.os.Bundle;
import com.avoscloud.chat.service.LoginFinishReceiver;

/**
 * Created by lzw on 14/11/20.
 */
public class BaseEntryActivity extends BaseActivity {
  LoginFinishReceiver loginFinishReceiver;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    loginFinishReceiver = LoginFinishReceiver.register(this);
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    unregisterReceiver(loginFinishReceiver);
  }
}
