package com.lzw.talk.ui.activity;

import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.MenuItem;
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

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case android.R.id.home:
        super.onBackPressed();
        return true;
    }
    return super.onOptionsItemSelected(item);
  }

  void hideSoftInputView() {
    Utils.hideSoftInputView(this);
  }

  void setSoftInputMode() {
    getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
  }

  void initActionBar(){
    initActionBar(null);
  }

  void initActionBar(String title) {
    ActionBar actionBar = getActionBar();
    if(title!=null){
      actionBar.setTitle(title);
    }
    actionBar.setDisplayUseLogoEnabled(false);
    actionBar.setDisplayHomeAsUpEnabled(true);
  }
}
