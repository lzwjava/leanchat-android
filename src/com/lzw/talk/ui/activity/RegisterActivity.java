package com.lzw.talk.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import com.lzw.talk.R;
import com.lzw.talk.base.App;
import com.lzw.talk.base.C;
import com.lzw.talk.service.UserService;
import com.lzw.talk.util.ChatUtils;
import com.lzw.talk.util.NetAsyncTask;
import com.lzw.talk.util.Utils;

public class RegisterActivity extends BaseActivity {
  Button registerButton;
  EditText usernameEdit, passwordEdit, emailEdit;
  RadioGroup sexRadio;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    // TODO Auto-generated method stub
    super.onCreate(savedInstanceState);
    setContentView(R.layout.register_activity);
    findView();
    initActionBar(App.ctx.getString(R.string.register));
    registerButton.setOnClickListener(new OnClickListener() {

      @Override
      public void onClick(View arg0) {
        // TODO Auto-generated method stub
        register();
      }
    });
  }

  private void findView() {
    usernameEdit = (EditText) findViewById(R.id.usernameEdit);
    passwordEdit = (EditText) findViewById(R.id.passwordEdit);
    emailEdit = (EditText) findViewById(R.id.ensurePasswordEdit);
    registerButton = (Button) findViewById(R.id.btn_register);
    sexRadio = (RadioGroup) findViewById(R.id.sexRadio);
  }

  private void register() {
    final String name = usernameEdit.getText().toString();
    final String password = passwordEdit.getText().toString();
    String againPassword = emailEdit.getText().toString();
    if (TextUtils.isEmpty(name)) {
      Utils.toast(R.string.username_cannot_null);
      return;
    }

    if (TextUtils.isEmpty(password)) {
      Utils.toast(R.string.password_can_not_null);
      return;
    }
    if (!againPassword.equals(password)) {
      Utils.toast(R.string.password_not_consistent);
      return;
    }

    int checkedId = sexRadio.getCheckedRadioButtonId();
    final boolean isMale;
    if (checkedId == R.id.male) {
      isMale = true;
    } else {
      isMale = false;
    }

    new NetAsyncTask(ctx) {
      @Override
      protected void doInBack() throws Exception {
        UserService.signUp(name, isMale, password);
      }

      @Override
      protected void onPost(Exception e) {
        if (e != null) {
          Utils.toast(App.ctx.getString(R.string.registerFailed) + e.getMessage());
        } else {
          Utils.toast(R.string.registerSucceed);
          ChatUtils.updateUserLocation();
          sendBroadcast(new Intent(C.ACTION_REGISTER_FINISH));
          Intent intent = new Intent(ctx, MainActivity.class);
          startActivity(intent);
          finish();
        }
      }
    }.execute();
  }

}
