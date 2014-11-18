package com.avoscloud.chat.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import com.avos.avoscloud.Group;
import com.avos.avoscloud.Session;
import com.avoscloud.chat.adapter.GroupAdapter;
import com.avoscloud.chat.avobject.ChatGroup;
import com.avoscloud.chat.entity.RoomType;
import com.avoscloud.chat.service.ChatService;
import com.avoscloud.chat.service.listener.GroupEventListener;
import com.avoscloud.chat.util.SimpleNetTask;
import com.avoscloud.chat.R;
import com.avoscloud.chat.base.App;
import com.avoscloud.chat.service.receiver.GroupMsgReceiver;
import com.avoscloud.chat.service.GroupService;
import com.avoscloud.chat.util.NetAsyncTask;
import com.avoscloud.chat.util.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by lzw on 14-10-7.
 */
public class GroupListActivity extends BaseActivity implements GroupEventListener, AdapterView.OnItemClickListener {
  public static final int GROUP_NAME_REQUEST = 0;
  ListView groupListView;
  List<ChatGroup> chatGroups = new ArrayList<ChatGroup>();
  GroupAdapter groupAdapter;
  String newGroupName;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.group_list_activity);
    findView();
    initList();
    refresh();
    initActionBar(App.ctx.getString(R.string.group));
    GroupMsgReceiver.addListener(this);
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    MenuInflater menuInflater = getMenuInflater();
    menuInflater.inflate(R.menu.group_list_menu, menu);
    return super.onCreateOptionsMenu(menu);
  }

  @Override
  public boolean onMenuItemSelected(int featureId, MenuItem item) {
    int id = item.getItemId();
    if (id == R.id.create) {
      UpdateContentActivity.goActivityForResult(this, App.ctx.getString(R.string.groupName), GROUP_NAME_REQUEST);
    }
    return super.onMenuItemSelected(featureId, item);
  }

  private void initList() {
    groupAdapter = new GroupAdapter(ctx, chatGroups);
    groupListView.setAdapter(groupAdapter);
    groupListView.setOnItemClickListener(this);
  }

  private void refresh() {
    new SimpleNetTask(ctx) {
      List<ChatGroup> subChatGroups;

      @Override
      protected void doInBack() throws Exception {
        subChatGroups = GroupService.findGroups();
      }

      @Override
      protected void onSucceed() {
        chatGroups.clear();
        chatGroups.addAll(subChatGroups);
        App.registerChatGroupsCache(chatGroups);
        groupAdapter.notifyDataSetChanged();
      }
    }.execute();
  }

  private void findView() {
    groupListView = (ListView) findViewById(R.id.groupList);
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (resultCode == RESULT_OK) {
      if (requestCode == GROUP_NAME_REQUEST) {
        newGroupName = UpdateContentActivity.getResultValue(data);
        Session session = ChatService.getSession();
        Group group = session.getGroup();
        group.join();
      }
    }
    super.onActivityResult(requestCode, resultCode, data);
  }


  @Override
  public void onJoined(final Group group) {
    //new Group
    if (newGroupName != null) {
      new NetAsyncTask(ctx) {
        ChatGroup chatGroup;

        @Override
        protected void doInBack() throws Exception {
          chatGroup = GroupService.setNewChatGroupData(group.getGroupId(), newGroupName);
        }

        @Override
        protected void onPost(Exception e) {
          newGroupName = null;
          if (e != null) {
            Utils.toast(e.getMessage());
            Utils.printException(e);
          } else {
            chatGroups.add(0, chatGroup);
            App.registerChatGroupsCache(Arrays.asList(chatGroup));
            groupAdapter.notifyDataSetChanged();
          }
        }
      }.execute();
    } else {
      ChatGroup _chatGroup = findChatGroup(group.getGroupId());
      if (_chatGroup == null) {
        throw new RuntimeException("chat group is null");
      }
      ChatActivity.goGroupChat(this,_chatGroup.getObjectId());
    }
  }

  @Override
  public void onMemberJoin(Group group, List<String> joinedPeerIds) {

  }

  @Override
  public void onMemberLeft(Group group, List<String> leftPeerIds) {

  }

  @Override
  public void onQuit(Group group) {
    refresh();
  }

  private ChatGroup findChatGroup(String groupId) {
    for (ChatGroup chatGroup : chatGroups) {
      if (chatGroup.getObjectId().equals(groupId)) {
        return chatGroup;
      }
    }
    return null;
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    GroupMsgReceiver.removeListener(this);
  }

  @Override
  public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
    ChatGroup chatGroup = (ChatGroup) parent.getAdapter().getItem(position);
    Group group = ChatService.getGroupById(chatGroup.getObjectId());
    group.join();
  }
}
