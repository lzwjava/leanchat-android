package com.avoscloud.leanchatlib_demo;

import android.os.Bundle;
import android.view.View;
import com.avoscloud.leanchatlib.activity.ChatActivity;

/**
 * Created by lzw on 15/4/27.
 */
public class ChatRoomActivity extends ChatActivity {

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    addLocationBtn.setVisibility(View.GONE);
  }
}
