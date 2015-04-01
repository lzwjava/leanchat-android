package com.avoscloud.chat.ui.activity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import com.avos.avoscloud.AVUser;
import com.avoscloud.chat.R;
import com.avoscloud.chat.service.UserService;
import com.avoscloud.chat.util.NetAsyncTask;
import com.avoscloud.chat.util.Utils;


public class EntryLoginActivity extends EntryBaseActivity implements OnClickListener {
  EditText usernameEdit, passwordEdit;
  Button loginBtn;
  TextView registerBtn;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    // TODO Auto-generated method stub
    super.onCreate(savedInstanceState);
    setContentView(R.layout.entry_login_activity);
    init();
  }

  private void init() {
    usernameEdit = (EditText) findViewById(R.id.et_username);
    passwordEdit = (EditText) findViewById(R.id.et_password);
    loginBtn = (Button) findViewById(R.id.btn_login);
    registerBtn = (TextView) findViewById(R.id.btn_register);
    loginBtn.setOnClickListener(this);
    registerBtn.setOnClickListener(this);
  }

  @Override
  public void onClick(View v) {
    if (v == registerBtn) {
      Utils.goActivity(ctx, EntryRegisterActivity.class);
    } else {
      login();
    }
  }

  private void login() {
    final String name = usernameEdit.getText().toString();
    final String password = passwordEdit.getText().toString();

    if (TextUtils.isEmpty(name)) {
      Utils.toast(R.string.username_cannot_null);
      return;
    }

    if (TextUtils.isEmpty(password)) {
      Utils.toast(R.string.password_can_not_null);
      return;
    }

    new NetAsyncTask(ctx) {
      @Override
      protected void doInBack() throws Exception {
        AVUser.logIn(name, password);
      }

      @Override
      protected void onPost(Exception e) {
        if (e != null) {
          Utils.toast(e.getMessage());
        } else {
          UserService.updateUserLocation();
          MainActivity.goMainActivity(EntryLoginActivity.this);
        }
      }
    }.execute();

  }
}
