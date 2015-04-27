package com.avoscloud.chat.ui.entry;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.LogInCallback;
import com.avoscloud.chat.R;
import com.avoscloud.chat.service.UserService;
import com.avoscloud.chat.ui.MainActivity;
import com.avoscloud.chat.util.Utils;


public class EntryLoginActivity extends EntryBaseActivity implements OnClickListener {
  private EditText usernameEdit, passwordEdit;
  private Button loginBtn;
  private TextView registerBtn;

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

    final ProgressDialog spinner = showSpinnerDialog();
    AVUser.logInInBackground(name, password, new LogInCallback<AVUser>() {
      @Override
      public void done(AVUser avUser, AVException e) {
        spinner.dismiss();
        if (filterException(e)) {
          UserService.updateUserLocation();
          MainActivity.goMainActivityFromActivity(EntryLoginActivity.this);
        }
      }
    });
  }
}
