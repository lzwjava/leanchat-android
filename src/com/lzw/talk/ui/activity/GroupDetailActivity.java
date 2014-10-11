package com.lzw.talk.ui.activity;

import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import com.lzw.talk.R;
import com.lzw.talk.adapter.GroupUsersAdapter;
import com.lzw.talk.avobject.ChatGroup;
import com.lzw.talk.avobject.User;
import com.lzw.talk.base.App;
import com.lzw.talk.service.ChatService;
import com.lzw.talk.util.SimpleNetTask;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lzw on 14-10-11.
 */
public class GroupDetailActivity extends BaseActivity implements AdapterView.OnItemClickListener {
  public static final String GROUP_ID = "groupId";
  public static final int ADD_MEMBERS = 0;
  ChatGroup chatGroup;
  GridView usersGrid;
  GroupUsersAdapter usersAdapter;
  List<User> members = new ArrayList<User>();
  boolean isOwner;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.group_detail_activity);
    initData();
    findView();
    initActionBar(chatGroup.getName());
    initGrid();
    refresh();
  }

  public static void goGroupDetail(Context ctx, String groupId) {
    Intent intent = new Intent(ctx, GroupDetailActivity.class);
    intent.putExtra(GROUP_ID, groupId);
    ctx.startActivity(intent);
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    if(isOwner){
      menu.add(0, ADD_MEMBERS, 0, R.string.add_members);
    }
    return super.onCreateOptionsMenu(menu);
  }

  @Override
  public boolean onMenuItemSelected(int featureId, MenuItem item) {
    int menuId=item.getItemId();
    if(menuId==ADD_MEMBERS){

    }
    return super.onMenuItemSelected(featureId, item);
  }

  private void refresh() {
    new SimpleNetTask(ctx) {
      List<User> subMembers = new ArrayList<User>();

      @Override
      protected void doInBack() throws Exception {
        subMembers = ChatService.findGroupMembers(chatGroup);
      }

      @Override
      protected void onSucceed() {
        usersAdapter.addAll(subMembers);
      }
    }.execute();
  }

  private void initGrid() {
    usersAdapter = new GroupUsersAdapter(ctx, members);
    usersGrid.setAdapter(usersAdapter);
    usersGrid.setOnItemClickListener(this);
  }

  private void initData() {
    String groupId = getIntent().getStringExtra(GROUP_ID);
    chatGroup = App.lookupChatGroup(groupId);
    isOwner = chatGroup.getOwner().equals(User.curUser());
  }

  private void findView() {
    usersGrid = (GridView) findViewById(R.id.usersGrid);
  }

  @Override
  public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
    User user = (User) parent.getAdapter().getItem(position);
    PersonInfoActivity.goPersonInfo(ctx, user.getUsername());
  }
}
