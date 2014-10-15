package com.avoscloud.chat.ui.activity;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;
import com.avoscloud.chat.adapter.NewFriendAdapter;
import com.avoscloud.chat.avobject.AddRequest;
import com.avoscloud.chat.service.PrefDao;
import com.avoscloud.chat.ui.view.dialog.DialogTips;
import com.avoscloud.chat.R;
import com.avoscloud.chat.avobject.User;
import com.avoscloud.chat.base.App;
import com.avoscloud.chat.service.AddRequestService;
import com.avoscloud.chat.util.NetAsyncTask;
import com.avoscloud.chat.util.SimpleNetTask;
import com.avoscloud.chat.util.Utils;

import java.util.ArrayList;
import java.util.List;

public class NewFriendActivity extends BaseActivity implements OnItemLongClickListener {
  ListView listview;
  NewFriendAdapter adapter;
  List<AddRequest> addRequests = new ArrayList<AddRequest>();

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    // TODO Auto-generated method stub
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_new_friend);
    initView();
    refresh();
  }

  private void refresh() {
    new NetAsyncTask(ctx) {
      List<AddRequest> subAddRequests;

      @Override
      protected void doInBack() throws Exception {
        subAddRequests = AddRequestService.findAddRequests();
      }

      @Override
      protected void onPost(Exception e) {
        if (e != null) {
          e.printStackTrace();
          Utils.toast(ctx, R.string.pleaseCheckNetwork);
        } else {
          PrefDao prefDao = new PrefDao(ctx, User.curUserId());
          prefDao.setAddRequestN(subAddRequests.size());
          adapter.addAll(subAddRequests);
        }
      }
    }.execute();
  }

  private void initView() {
    initActionBar(R.string.new_friends);
    listview = (ListView) findViewById(R.id.newfriendList);
    listview.setOnItemLongClickListener(this);
    adapter = new NewFriendAdapter(this, addRequests);
    listview.setAdapter(adapter);
  }

  @Override
  public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int position,
                                 long arg3) {
    // TODO Auto-generated method stub
    AddRequest invite = (AddRequest) adapter.getItem(position);
    showDeleteDialog(position, invite);
    return true;
  }

  public void showDeleteDialog(final int position, final AddRequest addRequest) {
    DialogTips dialog = new DialogTips(this, addRequest.getFromUser().getUsername(),
        App.ctx.getString(R.string.deleteFriendRequest), App.ctx.getString(R.string.ok), true, true);
    dialog.SetOnSuccessListener(new DialogInterface.OnClickListener() {
      public void onClick(DialogInterface dialogInterface, int userId) {
        deleteAddRequest(position, addRequest);
      }
    });
    dialog.show();
  }

  private void deleteAddRequest(final int position, final AddRequest addRequest) {
    new SimpleNetTask(ctx) {
      @Override
      public void onSucceed() {
        adapter.remove(position);
      }

      @Override
      protected void doInBack() throws Exception {
        addRequest.delete();
      }
    }.execute();

  }

  @Override
  protected void onDestroy() {
    // TODO Auto-generated method stub
    super.onDestroy();
  }
}
