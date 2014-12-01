package com.avoscloud.chat.ui.activity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import com.avoscloud.chat.avobject.User;
import com.avoscloud.chat.util.ChatUtils;
import com.avoscloud.chat.util.Utils;
import com.avoscloud.chat.R;

public class SplashActivity extends BaseEntryActivity {
  private static final int GO_MAIN_MSG = 1;
  private static final int GO_LOGIN_MSG = 2;
  public static final int SPLASH_DURATION = 2000;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    // TODO Auto-generated method stub
    super.onCreate(savedInstanceState);
    setContentView(R.layout.entry_splash_layout);

    if (User.curUser() != null) {
      ChatUtils.updateUserInfo();
      handler.sendEmptyMessageDelayed(GO_MAIN_MSG, SPLASH_DURATION);
    } else {
      handler.sendEmptyMessageDelayed(GO_LOGIN_MSG, SPLASH_DURATION);
    }
  }

  private Handler handler = new Handler() {
    @Override
    public void handleMessage(Message msg) {
      switch (msg.what) {
        case GO_MAIN_MSG:
          MainActivity.goMainActivity(SplashActivity.this);
          break;
        case GO_LOGIN_MSG:
          Utils.goActivity(ctx, LoginActivity.class);
          break;
      }
    }
  };
}
