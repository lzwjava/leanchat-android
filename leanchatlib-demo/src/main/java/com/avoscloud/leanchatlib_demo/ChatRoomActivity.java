package com.avoscloud.leanchatlib_demo;

import android.os.Bundle;
import android.view.View;
import com.avos.avoscloud.im.v2.messages.AVIMLocationMessage;
import com.avoscloud.leanchatlib.activity.ChatActivity;

/**
 * Created by lzw on 15/4/27.
 */
public class ChatRoomActivity extends ChatActivity {

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    addLocationBtn.setVisibility(View.VISIBLE);
//    addLocationBtn.setVisibility(View.GONE);
  }

  @Override
  protected void onAddLocationButtonClicked(View v) {
    toast("这里可以跳转到地图界面，选取地址");
  }

  @Override
  protected void onLocationMessageViewClicked(AVIMLocationMessage locationMessage) {
    toast("onLocationMessageViewClicked");
  }
}
