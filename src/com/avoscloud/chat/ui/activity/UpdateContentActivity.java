package com.avoscloud.chat.ui.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import com.avoscloud.chat.R;

/**
 * Created by lzw on 14-9-17.
 */
public class UpdateContentActivity extends BaseActivity {
  public static final String FIELD_NAME = "fieldName";
  public static final String VALUE = "value";
  TextView fieldNameView;
  EditText valueEdit;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.update_content_layout);
    findView();
    init();
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.update_content_menu, menu);
    return super.onCreateOptionsMenu(menu);
  }

  public static void goActivityForResult(Activity activity, String fieldName, int requestCode) {
    Intent intent = new Intent(activity, UpdateContentActivity.class);
    intent.putExtra(FIELD_NAME, fieldName);
    activity.startActivityForResult(intent, requestCode);
  }

  public static String getResultValue(Intent data) {
    return data.getStringExtra(VALUE);
  }

  private void init() {
    Intent intent = getIntent();
    String fieldName = intent.getStringExtra(FIELD_NAME);
    String editHint = ctx.getString(R.string.please_input_hint);
    String changeTitle = ctx.getString(R.string.change_title);
    editHint = editHint.replace("{0}", fieldName);
    changeTitle = changeTitle.replace("{0}", fieldName);
    fieldNameView.setText(fieldName);
    valueEdit.setHint(editHint);
    initActionBar(changeTitle);
  }

  public void updateContent() {
    Intent i = new Intent();
    i.putExtra(VALUE, valueEdit.getText().toString());
    setResult(RESULT_OK, i);
    finish();
  }

  @Override
  public boolean onMenuItemSelected(int featureId, MenuItem item) {
    int id = item.getItemId();
    if (id == R.id.sure) {
      updateContent();
    }
    return super.onMenuItemSelected(featureId, item);
  }

  private void findView() {
    fieldNameView = (TextView) findViewById(R.id.fieldName);
    valueEdit = (EditText) findViewById(R.id.valueEdit);
  }
}
