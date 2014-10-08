package com.lzw.talk.ui.activity;

import android.os.Bundle;
import android.widget.ListView;
import com.lzw.talk.R;
import com.lzw.talk.adapter.GroupAdapter;
import com.lzw.talk.entity.Group;
import com.lzw.talk.service.GroupService;
import com.lzw.talk.util.SimpleNetTask;

import java.util.List;

/**
 * Created by lzw on 14-10-7.
 */
public class GroupActivity extends BaseActivity {
  ListView groupListView;
  List<Group> groups;
  GroupAdapter groupAdapter;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.group_list_activity);
    findView();
    initList();
    refresh();
  }

  private void initList() {
    groupAdapter = new GroupAdapter(ctx, groups);
    groupListView.setAdapter(groupAdapter);
  }

  private void refresh() {
    new SimpleNetTask(ctx) {
      List<Group> subGroups;

      @Override
      protected void doInBack() throws Exception {
        subGroups = GroupService.findGroups();
      }

      @Override
      protected void onSucceed() {
        groupAdapter.addAll(subGroups);
        groupAdapter.notifyDataSetChanged();
      }
    }.execute();
  }

  private void findView() {
    groupListView = (ListView) findViewById(R.id.groupList);
  }
}
