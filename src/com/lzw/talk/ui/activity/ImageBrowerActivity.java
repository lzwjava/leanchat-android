package com.lzw.talk.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import com.lzw.talk.R;
import com.lzw.talk.adapter.ChatMsgAdapter;

/**
 * Created by lzw on 14-9-21.
 */
public class ImageBrowerActivity extends BaseActivity {
  String uri;
  ImageView imageView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.chat_image_brower_layout);
    imageView = (ImageView) findViewById(R.id.imageView);
    Intent intent = getIntent();
    uri = intent.getStringExtra("uri");
    ChatMsgAdapter.displayImageByUri(imageView, uri);
  }
}
