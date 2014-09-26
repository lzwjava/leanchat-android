package com.lzw.talk.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import com.lzw.talk.R;
import com.lzw.talk.avobject.User;
import com.lzw.talk.base.App;
import com.lzw.talk.service.ChatService;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by lzw on 14-9-21.
 */
public class SplashActivity extends BaseActivity {
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.splash_layout);
    int time = 1000;
    if (App.debug) {
      time = 0;
    }
    new Timer().schedule(new TimerTask() {
      @Override
      public void run() {
        if (User.curUser() != null) {
          ChatService.openSession();
        } else {
          Intent intent = new Intent(ctx, LoginActivity.class);
          startActivity(intent);
        }
      }
    }, time);
  }
}
