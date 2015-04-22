package com.avoscloud.chat.ui.contact;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import com.avos.avoscloud.AVUser;
import com.avoscloud.chat.R;
import com.avoscloud.chat.adapter.AddFriendAdapter;
import com.avoscloud.chat.base.App;
import com.avoscloud.chat.service.AddRequestService;
import com.avoscloud.chat.service.UserService;
import com.avoscloud.chat.ui.base_activity.BaseActivity;
import com.avoscloud.chat.ui.view.BaseListView;

import java.util.ArrayList;
import java.util.List;

public class AddFriendActivity extends BaseActivity implements AddFriendAdapter.ClickListener {
  @InjectView(R.id.searchNameEdit)
  EditText searchNameEdit;

  @InjectView(R.id.searchList)
  BaseListView<AVUser> listView;
  private String searchName = "";
  private AddFriendAdapter adapter;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    // TODO Auto-generated method stub
    super.onCreate(savedInstanceState);
    setContentView(R.layout.contact_add_friend_activity);
    ButterKnife.inject(this);
    init();
  }

  private void init() {
    initActionBar(App.ctx.getString(R.string.findFriends));
    adapter = new AddFriendAdapter(this, new ArrayList<AVUser>());
    listView.init(new BaseListView.DataFactory<AVUser>() {
      @Override
      public List<AVUser> getDatasInBackground(int skip, int limit, List<AVUser> currentDatas) throws Exception {
        return UserService.searchUser(searchName, adapter.getCount());
      }
    }, adapter);
    adapter.setClickListener(this);
    listView.onRefresh();
  }

  @OnClick(R.id.searchBtn)
  public void search(View view) {
    searchName = searchNameEdit.getText().toString();
    listView.onRefresh();
  }

  @Override
  public void onAddButtonClick(final AVUser user) {
    AddRequestService.createAddRequestInBackground(this, user);
  }
}
