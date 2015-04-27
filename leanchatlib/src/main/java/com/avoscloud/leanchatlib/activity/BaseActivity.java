package com.avoscloud.leanchatlib.activity;

import android.app.ActionBar;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;
import com.avoscloud.leanchatlib.R;

/**
 * Created by lzw on 14-9-17.
 */
public class BaseActivity extends FragmentActivity {
  protected Context ctx;

  protected void alwaysShowMenuItem(MenuItem add) {
    add.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS
        | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
  }

  protected ProgressDialog showSpinnerDialog() {
    //activity = modifyDialogContext(activity);
    ProgressDialog dialog = new ProgressDialog(this);
    dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
    dialog.setCancelable(true);
    dialog.setMessage(getString(R.string.chat_utils_hardLoading));
    if (!isFinishing()) {
      dialog.show();
    }
    return dialog;
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
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

  protected void hideSoftInputView() {
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

  protected void setSoftInputMode() {
    getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
  }

  protected void initActionBar() {
    initActionBar(null);
  }

  protected void initActionBar(String title) {
    ActionBar actionBar = getActionBar();
    if (actionBar == null) {
      throw new NullPointerException("action bar is null");
    }
    if (title != null) {
      actionBar.setTitle(title);
    }
    actionBar.setDisplayUseLogoEnabled(false);
    actionBar.setDisplayHomeAsUpEnabled(true);
  }

  protected void initActionBar(int id) {
    initActionBar(getString(id));
  }

  protected void toast(String str) {
    Toast.makeText(this, str, Toast.LENGTH_SHORT).show();
  }

  protected void toast(int id) {
    Toast.makeText(this, id, Toast.LENGTH_SHORT).show();
  }

  protected boolean filterException(Exception e) {
    if (e != null) {
      toast(e.getMessage());
      return false;
    } else {
      return true;
    }
  }
}
