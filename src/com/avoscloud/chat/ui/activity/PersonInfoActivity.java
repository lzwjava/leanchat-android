package com.avoscloud.chat.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.*;
import com.avos.avoscloud.AVException;
import com.avoscloud.chat.R;
import com.avoscloud.chat.avobject.User;
import com.avoscloud.chat.base.App;
import com.avoscloud.chat.service.CloudService;
import com.avoscloud.chat.service.UserService;
import com.avoscloud.chat.util.NetAsyncTask;
import com.avoscloud.chat.util.Utils;

import java.util.List;

public class PersonInfoActivity extends BaseActivity implements OnClickListener {
  public static final String USER_ID = "userId";
  TextView usernameView, genderView;
  ImageView avatarView, avatarArrowView;
  LinearLayout allLayout;
  Button chatBtn, addFriendBtn;
  RelativeLayout avatarLayout,  genderLayout;

  String userId = "";
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
    setContentView(R.layout.contact_person_info_activity);
    initData();

    findView();
    initView();
  }

  private void initData() {
    userId = getIntent().getStringExtra(USER_ID);
    user = App.lookupUser(userId);
  }

  private void findView() {
    allLayout = (LinearLayout) findViewById(R.id.all_layout);
    avatarView = (ImageView) findViewById(R.id.avatar_view);
    avatarArrowView = (ImageView) findViewById(R.id.avatar_arrow);
    usernameView = (TextView) findViewById(R.id.username_view);
    avatarLayout = (RelativeLayout) findViewById(R.id.head_layout);
    genderLayout = (RelativeLayout) findViewById(R.id.sex_layout);

    genderView = (TextView) findViewById(R.id.sexView);
    chatBtn = (Button) findViewById(R.id.chatBtn);
    addFriendBtn = (Button) findViewById(R.id.addFriendBtn);
  }

  private void initView() {
    User curUser = User.curUser();
    if (curUser.equals(user)) {
      initActionBar(R.string.personalInfo);
      avatarLayout.setOnClickListener(this);
      genderLayout.setOnClickListener(this);
      avatarArrowView.setVisibility(View.VISIBLE);
      chatBtn.setVisibility(View.GONE);
      addFriendBtn.setVisibility(View.GONE);
    } else {
      initActionBar(R.string.detailInfo);
      avatarArrowView.setVisibility(View.INVISIBLE);
      try {
        List<User> cacheFriends = UserService.findFriends(true);
        boolean isFriend = cacheFriends.contains(user);
        if (isFriend) {
          chatBtn.setVisibility(View.VISIBLE);
          chatBtn.setOnClickListener(this);
        } else {
          chatBtn.setVisibility(View.GONE);
          addFriendBtn.setVisibility(View.VISIBLE);
          addFriendBtn.setOnClickListener(this);
        }
      } catch (AVException e) {
        e.printStackTrace();
      }

    }
    updateView(user);
  }

  public static void goPersonInfo(Context ctx, String userId) {
    Intent intent = new Intent(ctx, PersonInfoActivity.class);
    intent.putExtra(USER_ID, userId);
    ctx.startActivity(intent);
  }

  private void updateView(User user) {
    String avatar = user.getAvatarUrl();
    UserService.displayAvatar(avatar, avatarView);
    usernameView.setText(user.getUsername());
    genderView.setText(user.getGenderDesc());
  }

  @Override
  public void onClick(View v) {
    // TODO Auto-generated method stub
    switch (v.getId()) {
      case R.id.chatBtn:// 发起聊天
        ChatActivity.goUserChat(this, user.getObjectId());
        finish();
        break;
      case R.id.addFriendBtn:// 添加好友
        new NetAsyncTask(ctx) {
          @Override
          protected void doInBack() throws Exception {
            CloudService.tryCreateAddRequest(user);
          }

          @Override
          protected void onPost(Exception e) {
            if (e != null) {
              Utils.toast(e.getMessage());
            } else {
              Utils.toast(R.string.sendRequestSucceed);
            }
          }
        }.execute();
        break;
    }
  }
}
