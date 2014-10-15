package com.avoscloud.chat.ui.activity;

import android.os.Bundle;
import com.avoscloud.chat.R;

/**
 * Created by lzw on 14-9-24.
 */
public class NotifyPrefActivity extends BaseActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.notify_pref_layout);
    initActionBar(R.string.notifySetting);
  }
}
