package com.avoscloud.chat.ui.activity;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import com.avoscloud.chat.R;
import com.avoscloud.chat.adapter.GroupAddMembersAdapter;
import com.avoscloud.chat.avobject.ChatGroup;
import com.avoscloud.chat.avobject.User;
import com.avoscloud.chat.base.App;
import com.avoscloud.chat.service.GroupService;
import com.avoscloud.chat.util.SimpleNetTask;
import com.avoscloud.chat.util.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lzw on 14-10-11.
 */
public class GroupAddMembersActivity extends BaseActivity {
  public static final int OK = 0;
  GroupAddMembersAdapter adapter;
  ListView userList;
  List<User> users;
  public static ChatGroup chatGroup;
  public static List<User> members;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.group_add_members_layout);
    findView();
    initData();
    initList();
    initActionBar();
  }

  private void initData() {
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    MenuItem add = menu.add(0, OK, 0, R.string.sure);
    Utils.alwaysShowMenuItem(add);
    return super.onCreateOptionsMenu(menu);
  }

  @Override
  public boolean onMenuItemSelected(int featureId, MenuItem item) {
    int id = item.getItemId();
    if (id == OK) {
      addMembers();
    }
    return super.onMenuItemSelected(featureId, item);
  }

  private void addMembers() {
    final List<User> checkedUsers = adapter.getCheckedDatas();
    new SimpleNetTask(ctx) {
      @Override
      protected void doInBack() throws Exception {
        GroupService.inviteMembers(chatGroup, checkedUsers);
      }

      @Override
      protected void onSucceed() {
        Utils.toast(R.string.inviteSucceed);
        finish();
      }
    }.execute();
  }

  private void initList() {
    App app = App.getInstance();
    users = app.getFriends();
    List<User> restUsers = removeMembers(users, members);
    adapter = new GroupAddMembersAdapter(ctx, restUsers);
    userList.setAdapter(adapter);
  }

  private List<User> removeMembers(List<User> users, List<User> members) {
    List<User> restUsers = new ArrayList<User>(users);
    restUsers.removeAll(members);
    return restUsers;
  }

  private void findView() {
    userList = (ListView) findViewById(R.id.userList);
  }

}
