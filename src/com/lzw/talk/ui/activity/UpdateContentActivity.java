package com.lzw.talk.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import com.lzw.talk.R;
import com.lzw.talk.ui.view.HeaderLayout;

/**
 * Created by lzw on 14-9-17.
 */
public class UpdateContentActivity extends BaseActivity {
  public static final String FIELD_NAME = "fieldName";
  public static final String VALUE = "value";
  HeaderLayout headerLayout;
  TextView fieldNameView;
  EditText valueEdit;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.update_content_layout);
    findView();
    init();
  }

  public static void goActivityForResult(Activity ctx, String fieldName, int requestCode) {
    Intent intent = new Intent(ctx, UpdateContentActivity.class);
    intent.putExtra(FIELD_NAME, fieldName);
    ctx.startActivityForResult(intent, requestCode);
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
    headerLayout.showTitle(changeTitle);
    headerLayout.showLeftBackButton(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        setResult(RESULT_CANCELED);
        finish();
      }
    });
    headerLayout.showRightTextButton(R.string.sure, new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Intent i = new Intent();
        i.putExtra(VALUE, valueEdit.getText().toString());
        setResult(RESULT_OK, i);
        finish();
      }
    });
  }

  private void findView() {
    headerLayout = (HeaderLayout) findViewById(R.id.headerLayout);
    fieldNameView = (TextView) findViewById(R.id.fieldName);
    valueEdit = (EditText) findViewById(R.id.valueEdit);
  }
}
