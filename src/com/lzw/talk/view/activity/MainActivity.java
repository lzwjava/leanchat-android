package com.lzw.talk.view.activity;

import android.os.Bundle;
import android.view.View;
import com.lzw.talk.R;
import com.lzw.talk.view.HeaderLayout;

/**
 * Created by lzw on 14-9-17.
 */
public class MainActivity extends BaseActivity {
  HeaderLayout headerLayout;
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    findView();
    headerLayout.setTitle(R.string.app_name);
  }

  private void findView() {
    headerLayout= (HeaderLayout) findViewById(R.id.headerLayout);
  }

  public void onTabSelect(View v) {
    int id=v.getId();
  }
}
