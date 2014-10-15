package com.avoscloud.chat.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import com.avoscloud.chat.R;

/**
 * Created by lzw on 14-10-9.
 */
public class GroupAddDialog extends BaseActivity implements View.OnClickListener {
  public static final int SEARCH = 0;
  public static final int CREATE = 1;
  public static final String ACTION = "action";
  View search, create;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.group_add_dialog);
    findView();
  }

  private void findView() {
    search = findViewById(R.id.search);
    create = findViewById(R.id.create);
    search.setOnClickListener(this);
    create.setOnClickListener(this);
  }

  @Override
  public boolean onTouchEvent(MotionEvent event) {
    setResult(RESULT_CANCELED);
    finish();
    return super.onTouchEvent(event);
  }

  @Override
  public void onClick(View v) {
    int id = v.getId();
    Intent intent = new Intent();
    int action = 0;
    if (id == R.id.search) {
      action = SEARCH;
    } else if (id == R.id.create) {
      action = CREATE;
    }
    intent.putExtra(ACTION, action);
    setResult(RESULT_OK, intent);
    finish();
  }
}
