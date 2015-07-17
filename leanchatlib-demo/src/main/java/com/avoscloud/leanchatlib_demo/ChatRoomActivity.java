package com.avoscloud.leanchatlib_demo;

import android.os.Bundle;
import android.view.View;
import com.avos.avoscloud.im.v2.messages.AVIMImageMessage;
import com.avos.avoscloud.im.v2.messages.AVIMLocationMessage;
import com.avoscloud.leanchatlib.activity.ChatActivity;
import com.avoscloud.leanchatlib.activity.ChatActivityEventListener;

/**
 * Created by lzw on 15/4/27.
 */
public class ChatRoomActivity extends ChatActivity implements ChatActivityEventListener {

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    addLocationBtn.setVisibility(View.VISIBLE);
//    addLocationBtn.setVisibility(View.GONE);
  }

  @Override
  public void onAddLocationButtonClicked(View v) {
    toast("这里可以跳转到地图界面，选取地址");
  }

  @Override
  public void onLocationMessageViewClicked(AVIMLocationMessage locationMessage) {
    toast("这里跳转到地图界面，查看地理位置");
  }

  @Override
  public void onImageMessageViewClicked(AVIMImageMessage imageMessage, String localImagePath) {
    toast("这里跳转到图片浏览页面，查看图片消息详情");
  }
}
