package com.lzw.talk.ui.activity;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

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
    if (getWindow().getAttributes().softInputMode !=
        WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN) {
      InputMethodManager manager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
      View currentFocus = getCurrentFocus();
      if (currentFocus != null) {
        manager.hideSoftInputFromWindow(currentFocus.getWindowToken(),
            InputMethodManager.HIDE_NOT_ALWAYS);
      }
    }
  }

  void setSoftInputMode() {
    getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
  }
}
