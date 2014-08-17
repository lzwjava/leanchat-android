package com.lzw.talk.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.Session;
import com.avos.avoscloud.SessionManager;
import com.lzw.commons.Logger;
import com.lzw.commons.NetAsyncTask;
import com.lzw.commons.Utils;
import com.lzw.talk.R;
import com.lzw.talk.avobject.User;
import com.lzw.talk.base.App;
import com.lzw.talk.service.ChatService;

import java.util.LinkedList;
import java.util.List;

public class LoginActivity extends Activity implements View.OnClickListener {
  /**
   * Called when the activity is first created.
   */
  EditText usernameEdit;
  View start;
  private Activity ctx;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    ctx =this;
    setContentView(R.layout.login);
    findView();
    if(User.curUser()!=null){
      loginSucceed();
    }
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
      final String username=usernameEdit.getText().toString();
      if(username.isEmpty()==false){
        new NetAsyncTask(ctx) {
          @Override
          protected void doInBack() throws Exception {
            AVUser avUser = User.getAVUser(username);
            if(avUser==null){
              AVUser user=new AVUser();
              user.put(User.USERNAME,username);
              user.put(User.PASSWORD,username);
              user.signUp();
            }else{
              AVUser.logIn(username,username);
            }
          }

          @Override
          protected void onPost(boolean res) {
            if(res){
              loginSucceed();
            }else{
              Utils.toast(cxt,R.string.usernameIsTokenOrBadNet);
            }
          }
        }.execute();
      }
    }
  }

  public void loginSucceed(){
    openSession();
    Intent intent=new Intent(ctx,UsersActivity.class);
    startActivity(intent);
  }


  private void openSession() {
    Logger.d("open SessionAndChat");
    final String selfId = ChatService.getPeerId(User.curUser());
    List<String> peerIds = new LinkedList<String>();
    Session session = SessionManager.getInstance(selfId);
    App.session = session;
    session.open(selfId, peerIds);
    finish();
  }
}
