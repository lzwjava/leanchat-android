package com.lzw.talk.view.activity;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import com.lzw.talk.R;

/**
 * Created by lzw on 14-9-17.
 */
public class MainActivity extends BaseActivity {
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
  }

  public void onTabSelect(View v) {
    int id=v.getId();

  }
}
