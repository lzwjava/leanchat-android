package com.lzw.talk.ui.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import com.lzw.talk.service.ChatService;
import com.lzw.talk.service.GroupService;
import com.lzw.talk.util.SimpleNetTask;
import com.lzw.talk.util.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lzw on 14-10-11.
 */
public class GroupDetailActivity extends BaseActivity implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {
  public static final int ADD_MEMBERS = 0;
  public static ChatGroup chatGroup;
  public static List<User> members = new ArrayList<User>();

  GridView usersGrid;
  GroupUsersAdapter usersAdapter;
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
    Class<?> clz = GroupDetailActivity.class;
    GroupService.goChatGroupActivity(ctx, clz, groupId);
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    if (isOwner) {
      menu.add(0, ADD_MEMBERS, 0, R.string.add_members);
    }
    return super.onCreateOptionsMenu(menu);
  }

  @Override
  public boolean onMenuItemSelected(int featureId, MenuItem item) {
    int menuId = item.getItemId();
    if (menuId == ADD_MEMBERS) {
      GroupAddMembersActivity.chatGroup = chatGroup;
      GroupAddMembersActivity.members = members;
      Utils.goActivity(ctx, GroupAddMembersActivity.class);
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
    usersGrid.setOnItemLongClickListener(this);
  }

  private void initData() {
    chatGroup = GroupService.getGroupByIntent(ctx.getIntent());
    isOwner = GroupService.isGroupOwner(chatGroup, User.curUser());
  }

  private void findView() {
    usersGrid = (GridView) findViewById(R.id.usersGrid);
  }

  @Override
  public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
    User user = (User) parent.getAdapter().getItem(position);
    PersonInfoActivity.goPersonInfo(ctx, user.getUsername());
  }

  @Override
  public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
    User user = (User) parent.getAdapter().getItem(position);
    boolean isTheOwner = GroupService.isGroupOwner(chatGroup, user);
    if (isTheOwner == false) {
      new AlertDialog.Builder(ctx).setMessage(R.string.kickTips)
          .setPositiveButton(R.string.sure, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
              GroupService.kickMembers(chatGroup,members);
            }
          }).setNegativeButton(R.string.cancel, null).show();
    }
    return true;
  }
}
