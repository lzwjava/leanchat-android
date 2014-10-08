package com.lzw.talk.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import com.avos.avoscloud.AVException;
import com.avos.avoscloud.Group;
import com.avos.avoscloud.SaveCallback;
import com.avos.avoscloud.Session;
import com.lzw.talk.R;
import com.lzw.talk.adapter.GroupAdapter;
import com.lzw.talk.avobject.ChatGroup;
import com.lzw.talk.base.App;
import com.lzw.talk.service.ChatService;
import com.lzw.talk.service.GroupListener;
import com.lzw.talk.service.GroupMsgReceiver;
import com.lzw.talk.service.GroupService;
import com.lzw.talk.ui.view.HeaderLayout;
import com.lzw.talk.util.Logger;
import com.lzw.talk.util.SimpleNetTask;
import com.lzw.talk.util.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by lzw on 14-10-7.
 */
public class GroupActivity extends BaseActivity implements GroupListener {
  public static final int GROUP_NAME_REQUEST = 0;
  ListView groupListView;
  List<ChatGroup> chatGroups = new ArrayList<ChatGroup>();
  GroupAdapter groupAdapter;
  HeaderLayout headerLayout;
  String newGroupName;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.group_list_activity);
    findView();
    initHeader();
    initList();
    refresh();
    GroupMsgReceiver.registerGroupListener(this);
  }

  private void initHeader() {
    headerLayout = (HeaderLayout) findViewById(R.id.headerLayout);
    headerLayout.showTitle(R.string.group);
    headerLayout.showRightImageButton(R.drawable.base_action_bar_add_bg_selector, new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        UpdateContentActivity.goActivityForResult(ctx, App.ctx.getString(R.string.groupName), GROUP_NAME_REQUEST);
      }
    });
  }

  private void initList() {
    groupAdapter = new GroupAdapter(ctx, chatGroups);
    groupListView.setAdapter(groupAdapter);
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
        Logger.d("request code " + requestCode);
        newGroupName = UpdateContentActivity.getResultValue(data);
        Session session = ChatService.getSession();
        Group group = session.getGroup();
        group.join();
      }
    }
    super.onActivityResult(requestCode, resultCode, data);
  }

  @Override
  public void onJoined(Group group) {
    final ChatGroup chatGroup;
    try {
      chatGroup = ChatGroup.createWithoutData(ChatGroup.class, group.getGroupId());
    } catch (AVException e) {
      e.printStackTrace();
      return;
    }
    //new Group
    if (newGroupName != null) {
      new SimpleNetTask(ctx) {
        @Override
        protected void doInBack() throws Exception {
          chatGroup.setName(newGroupName);
          chatGroup.setFetchWhenSave(true);
          chatGroup.save();
        }

        @Override
        protected void onSucceed() {
          chatGroups.add(0, chatGroup);
          App.registerChatGroupsCache(Arrays.asList(chatGroup));
          groupAdapter.notifyDataSetChanged();
          newGroupName = null;
        }
      }.execute();
    } else {
      ChatGroup _chatGroup = findChatGroup(group.getGroupId());
      if (_chatGroup == null) {
        throw new RuntimeException("chat group is null");
      }
      Intent intent = new Intent(ctx, ChatActivity.class);
    }
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
    GroupMsgReceiver.unregisterGroupListener();
  }
}
