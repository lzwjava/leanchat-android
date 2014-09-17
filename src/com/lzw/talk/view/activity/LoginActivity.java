package com.lzw.talk.view.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.Session;
import com.avos.avoscloud.SessionManager;
import com.lzw.talk.R;
import com.lzw.talk.avobject.User;
import com.lzw.talk.base.App;
import com.lzw.talk.service.ChatService;
import com.lzw.talk.service.UserManager;
import com.lzw.talk.util.NetAsyncTask;
import com.lzw.talk.util.Utils;

import java.util.LinkedList;
import java.util.List;

public class LoginActivity extends BaseActivity implements View.OnClickListener {
  /**
   * Called when the activity is first created.
   */
  EditText usernameEdit;
  View start;
  private Activity ctx;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    ctx = this;
    setContentView(R.layout.login);
    findView();
    if (User.curUser() != null) {
      loginSucceed();
    }
    headerLayout.showTitle(R.string.login);
  }

  private void findView() {
    usernameEdit = (EditText) findViewById(R.id.username);
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
            AVUser avUser = UserManager.getAVUser(username);
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
    openSession();
    Intent intent = new Intent(ctx, MainActivity.class);
    startActivity(intent);
    finish();
  }


  public void openSession() {
    final String selfId = ChatService.getPeerId(User.curUser());
    List<String> peerIds = new LinkedList<String>();
    Session session = SessionManager.getInstance(selfId);
    session.open(selfId, peerIds);
    App.session = session;
  }
}
