package com.lzw.talk.ui.activity;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.WindowManager;
import com.lzw.talk.util.Utils;

/**
 * Created by lzw on 14-9-17.
 */
public class BaseActivity extends FragmentActivity {
  Activity ctx;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    ctx = this;
  }

  void hideSoftInputView() {
    Utils.hideSoftInputView(this);
  }

  void setSoftInputMode() {
    getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
  }
}
