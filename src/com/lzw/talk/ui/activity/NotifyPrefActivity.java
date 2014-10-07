package com.lzw.talk.ui.activity;

import android.os.Bundle;
import com.lzw.talk.R;
import com.lzw.talk.ui.view.HeaderLayout;

/**
 * Created by lzw on 14-9-24.
 */
public class NotifyPrefActivity extends BaseActivity {
  HeaderLayout headerLayout;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.notify_pref_layout);
    headerLayout = (HeaderLayout) findViewById(R.id.headerLayout);
    headerLayout.showTitle(R.string.notifySetting);
    headerLayout.showLeftBackButton();
  }
}
