package com.lzw.talk.ui.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.*;
import com.avos.avoscloud.AVException;
import com.avos.avoscloud.FindCallback;
import com.lzw.talk.R;
import com.lzw.talk.adapter.AddFriendAdapter;
import com.lzw.talk.avobject.User;
import com.lzw.talk.base.App;
import com.lzw.talk.service.UserService;
import com.lzw.talk.ui.view.HeaderLayout;
import com.lzw.talk.util.Logger;
import com.lzw.talk.util.PhotoUtil;
import com.lzw.talk.util.Utils;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.List;

public class PersonInfoActivity extends BaseActivity implements OnClickListener {
  TextView usernameView, sexView;
  ImageView avatarView, avatarArrowView;
  LinearLayout allLayout;
  Button chatBtn, addFriendBtn;
  RelativeLayout avatarLayout, nickLayout, sexLayout;
  HeaderLayout headerLayout;

  String from = "";
  String username = "";
  User user;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    // TODO Auto-generated method stub
    super.onCreate(savedInstanceState);
    //meizu?
    int currentApiVersion = Build.VERSION.SDK_INT;
    if (currentApiVersion >= 14) {
      getWindow().getDecorView().setSystemUiVisibility(
          View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
    }
    setContentView(R.layout.activity_set_info);
    from = getIntent().getStringExtra("from");//me add other
    username = getIntent().getStringExtra("username");
    findView();
    initView();
  }

  @SuppressLint("WrongViewCast")
  private void findView() {
    allLayout = (LinearLayout) findViewById(R.id.all_layout);
    avatarView = (ImageView) findViewById(R.id.avatar_view);
    avatarArrowView = (ImageView) findViewById(R.id.avatar_arrow);
    usernameView = (TextView) findViewById(R.id.username_view);
    avatarLayout = (RelativeLayout) findViewById(R.id.head_layout);
    sexLayout = (RelativeLayout) findViewById(R.id.sex_layout);

    sexView = (TextView) findViewById(R.id.sexView);
    chatBtn = (Button) findViewById(R.id.chatBtn);
    addFriendBtn = (Button) findViewById(R.id.addFriendBtn);
    headerLayout = (HeaderLayout) findViewById(R.id.headerLayout);
  }

  private void initView() {
    addFriendBtn.setEnabled(false);
    chatBtn.setEnabled(false);
    if (from.equals("me")) {
      headerLayout.showTitle(R.string.personalInfo);
      headerLayout.showLeftBackButton();
      avatarLayout.setOnClickListener(this);
      nickLayout.setOnClickListener(this);
      sexLayout.setOnClickListener(this);
      avatarArrowView.setVisibility(View.VISIBLE);
      chatBtn.setVisibility(View.GONE);
      addFriendBtn.setVisibility(View.GONE);
    } else {
      headerLayout.showTitle(R.string.detailInfo);
      headerLayout.showLeftBackButton();
      avatarArrowView.setVisibility(View.INVISIBLE);
      if (from.equals("add")) {// 从附近的人列表添加好友--因为获取附近的人的方法里面有是否显示好友的情况，因此在这里需要判断下这个用户是否是自己的好友
        App app = App.getInstance();
        if (UserService.isMyFriend(app.getFriends(), username)) {// 是好友
          chatBtn.setVisibility(View.VISIBLE);
          chatBtn.setOnClickListener(this);
        } else {
          chatBtn.setVisibility(View.GONE);
          addFriendBtn.setVisibility(View.VISIBLE);
          addFriendBtn.setOnClickListener(this);
        }
      } else {// 查看他人
        chatBtn.setVisibility(View.VISIBLE);
        chatBtn.setOnClickListener(this);
      }
      initOtherData(username);
    }
  }

  private void initMeData() {
    User user = User.curUser();
    initOtherData(user.getUsername());
  }

  private void initOtherData(String name) {
    FindCallback<User> findCallback = new FindCallback<User>() {
      @Override
      public void done(List<User> users, AVException e) {
        if (e != null) {
          Utils.toast(e.getMessage());
        } else {
          if (users != null && users.size() > 0) {
            user = users.get(0);
            chatBtn.setEnabled(true);
            addFriendBtn.setEnabled(true);
            updateUser(user);
          } else {
            Logger.i("onSuccess 查无此人");
          }
        }
      }
    };
    UserService.findUserInfo(name, findCallback);
  }

  private void updateUser(User user) {
    refreshAvatar(user.getAvatarUrl());
    usernameView.setText(user.getUsername());
    sexView.setText(user.getSex() == true ? R.string.male : R.string.female);
  }

  private void refreshAvatar(String avatarUrl) {
    UserService.displayAvatar(avatarUrl,avatarView);
  }

  @Override
  public void onResume() {
    // TODO Auto-generated method stub
    super.onResume();
    if (from.equals("me")) {
      initMeData();
    }
  }

  @Override
  public void onClick(View v) {
    // TODO Auto-generated method stub
    switch (v.getId()) {
      case R.id.chatBtn:// 发起聊天
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra(ChatActivity.CHAT_USER_ID, user.getObjectId());
        startActivity(intent);
        finish();
        break;
      case R.id.addFriendBtn:// 添加好友
        addFriend();
        break;
    }
  }

  private void addFriend() {
    AddFriendAdapter.runAddFriendTask(ctx, user);
  }


}
