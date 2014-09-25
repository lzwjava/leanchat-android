package com.lzw.talk.ui.activity;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

/**
 * Created by lzw on 14-9-17.
 */
public class BaseActivity extends FragmentActivity{
  Activity ctx;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    ctx=this;
  }
}
