package com.avoscloud.leanchatlib_demo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.avos.avoscloud.AVException;
import com.avos.avoscloud.im.v2.AVIMClient;
import com.avos.avoscloud.im.v2.callback.AVIMClientCallback;
import com.avoscloud.leanchatlib.controller.ChatManager;


public class LoginActivity extends Activity implements View.OnClickListener {
  private EditText selfIdEditText;
  private Button loginButton;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_login);
    selfIdEditText = (EditText) findViewById(R.id.selfIdEditText);
    loginButton = (Button) findViewById(R.id.login);
    loginButton.setOnClickListener(this);
  }

  @Override
  public void onClick(View view) {
    String selfId = selfIdEditText.getText().toString();
    if (!TextUtils.isEmpty(selfId)) {
      ChatManager chatManager = ChatManager.getInstance();
      chatManager.setupDatabaseWithSelfId(selfId);
      chatManager.openClientWithSelfId(selfId, new AVIMClientCallback() {
        @Override
        public void done(AVIMClient avimClient, AVException e) {
          if (e != null) {
            e.printStackTrace();
          }
          Intent intent = new Intent(LoginActivity.this, MainActivity.class);
          startActivity(intent);
          finish();
        }
      });
    }
  }
}
