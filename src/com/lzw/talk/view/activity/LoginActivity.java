package com.lzw.talk.view.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import com.avos.avoscloud.AVUser;
import com.lzw.talk.R;
import com.lzw.talk.avobject.User;
import com.lzw.talk.service.ChatService;
import com.lzw.talk.service.UserService;
import com.lzw.talk.util.NetAsyncTask;
import com.lzw.talk.util.Utils;
import com.lzw.talk.view.HeaderLayout;

public class LoginActivity extends BaseActivity implements View.OnClickListener {
  /**
   * Called when the activity is first created.
   */
  EditText usernameEdit;
  View start;
  private Activity ctx;
  HeaderLayout headerLayout;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    ctx = this;
    setContentView(R.layout.login);
    findView();
    headerLayout.showTitle(R.string.login);
  }

  private void findView() {
    usernameEdit = (EditText) findViewById(R.id.username);
    headerLayout= (HeaderLayout) findViewById(R.id.headerLayout);
    start = findViewById(R.id.ok);
    start.setOnClickListener(this);
  }

  @Override
  public void onClick(View v) {
    int id = v.getId();
    if (id == R.id.ok) {
      final String username = usernameEdit.getText().toString();
      if (username.isEmpty() == false) {
        new NetAsyncTask(ctx) {
          @Override
          protected void doInBack() throws Exception {
            AVUser avUser = UserService.getAVUser(username);
            if (avUser == null) {
              AVUser user = new AVUser();
              user.put(User.USERNAME, username);
              user.put(User.PASSWORD, username);
              user.signUp();
            } else {
              AVUser.logIn(username, username);
            }
          }

          @Override
          protected void onPost(boolean res) {
            if (res) {
              loginSucceed();
            } else {
              Utils.toast(cxt, R.string.username_is_token_or_bad_net);
            }
          }
        }.execute();
      }
    }
  }

  public void loginSucceed() {
    ChatService.openSession();
  }
}
