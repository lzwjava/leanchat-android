package com.avoscloud.chat.ui.activity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import com.avos.avoscloud.AVException;
import com.avos.avoscloud.im.v2.AVIMConversation;
import com.avos.avoscloud.im.v2.callback.AVIMConversationCallback;
import com.avos.avoscloud.im.v2.callback.AVIMConversationCreatedCallback;
import com.avoscloud.chat.R;
import com.avoscloud.chat.adapter.GroupAddMembersAdapter;
import com.avoscloud.chat.entity.ConvType;
import com.avoscloud.chat.service.CacheService;
import com.avoscloud.chat.service.chat.ConvManager;
import com.avoscloud.chat.service.event.FinishEvent;
import com.avoscloud.chat.util.Utils;
import de.greenrobot.event.EventBus;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lzw on 14-10-11.
 */
public class ConvAddMembersActivity extends ConvBaseActivity {
  public static final int OK = 0;
  private GroupAddMembersAdapter adapter;
  private ListView userList;
  private ConvManager convManager;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.group_add_members_layout);
    findView();
    convManager = ConvManager.getInstance();
    initList();
    initActionBar();
    setListData();
  }

  @Override
  protected void onConvChanged(AVIMConversation conv) {
  }

  private void setListData() {
    List<String> ids = new ArrayList<String>();
    ids.addAll(CacheService.getFriendIds());
    ids.removeAll(conv().getMembers());
    adapter.setDatas(ids);
    adapter.notifyDataSetChanged();
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    MenuItem add = menu.add(0, OK, 0, R.string.sure);
    alwaysShowMenuItem(add);
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
    final List<String> checkedUsers = adapter.getCheckedDatas();
    final ProgressDialog dialog = Utils.showSpinnerDialog(this);
    if (checkedUsers.size() == 0) {
      finish();
    } else {
      if (ConvManager.typeOfConv(conv()) == ConvType.Single) {
        List<String> members = new ArrayList<String>();
        members.addAll(checkedUsers);
        members.addAll(conv().getMembers());
        convManager.createGroupConv(members, new AVIMConversationCreatedCallback() {
          @Override
          public void done(final AVIMConversation conversation, AVException e) {
            if (Utils.filterException(e)) {
              EventBus eventBus = EventBus.getDefault();
              FinishEvent finishEvent = new FinishEvent();
              eventBus.post(finishEvent);
              ChatActivity.goByConv(ConvAddMembersActivity.this, conversation);
            }
          }
        });
      } else {
        conv().addMembers(checkedUsers, new AVIMConversationCallback() {
          @Override
          public void done(AVException e) {
            dialog.dismiss();
            if (Utils.filterException(e)) {
              Utils.toast(R.string.inviteSucceed);
              convManager.postConvChanged(conv());
              finish();
            }
          }
        });
      }
    }
  }

  private void initList() {
    adapter = new GroupAddMembersAdapter(ctx, new ArrayList<String>());
    userList.setAdapter(adapter);
  }

  private void findView() {
    userList = (ListView) findViewById(R.id.userList);
  }

}
