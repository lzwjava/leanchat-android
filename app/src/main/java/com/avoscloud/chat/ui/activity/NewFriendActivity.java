package com.avoscloud.chat.ui.activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import butterknife.ButterKnife;
import butterknife.InjectView;
import com.avos.avoscloud.AVException;
import com.avos.avoscloud.SaveCallback;
import com.avoscloud.chat.R;
import com.avoscloud.chat.adapter.NewFriendAdapter;
import com.avoscloud.chat.avobject.AddRequest;
import com.avoscloud.chat.avobject.User;
import com.avoscloud.chat.service.AddRequestService;
import com.avoscloud.chat.service.PreferenceMap;
import com.avoscloud.chat.ui.view.BaseListView;
import com.avoscloud.chat.util.RefreshTask;
import com.avoscloud.chat.util.Refreshable;
import com.avoscloud.chat.util.Utils;

import java.util.ArrayList;
import java.util.List;

public class NewFriendActivity extends BaseActivity implements NewFriendAdapter.Listener,
    Refreshable {
  @InjectView(R.id.newfriendList)
  BaseListView<AddRequest> listView;
  NewFriendAdapter adapter;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    // TODO Auto-generated method stub
    super.onCreate(savedInstanceState);
    setContentView(R.layout.contact_new_friend_activity);
    ButterKnife.inject(this);
    initView();
    refresh();
  }

  public void refresh() {
    listView.onRefresh();
  }

  private void initView() {
    initActionBar(R.string.new_friends);
    adapter = new NewFriendAdapter(this);
    adapter.setListener(this);
    listView.init(new BaseListView.DataFactory<AddRequest>() {
      @Override
      public List<AddRequest> getDatas(int skip, int limit, List<AddRequest> currentDatas) throws Exception {
        List<AddRequest> addRequests = AddRequestService.findAddRequests();
        List<AddRequest> filters = new ArrayList<AddRequest>();
        for (AddRequest addRequest : addRequests) {
          if (addRequest.getFromUser() != null) {
            filters.add(addRequest);
          }
        }
        addRequests = filters;
        PreferenceMap preferenceMap = new PreferenceMap(ctx, User.getCurrentUserId());
        preferenceMap.setAddRequestN(addRequests.size());
        return addRequests;
      }
    }, adapter);
    listView.setPullLoadEnable(false);

    listView.setItemListener(new BaseListView.ItemListener<AddRequest>() {
      @Override
      public void onItemLongPressed(final AddRequest item) {
        new AlertDialog.Builder(ctx).setMessage(R.string.deleteFriendRequest)
            .setPositiveButton(R.string.sure, new DialogInterface.OnClickListener() {
              @Override
              public void onClick(DialogInterface dialog, int which) {
                deleteAddRequest(item);
              }
            }).setNegativeButton(R.string.cancel, null).show();
      }
    });
  }

  @Override
  public void onAgreeAddRequest(final AddRequest addRequest) {
    final ProgressDialog dialog = Utils.showSpinnerDialog(this);
    AddRequestService.agreeAddRequest(addRequest, new SaveCallback() {
      @Override
      public void done(AVException e) {
        dialog.dismiss();
        if (Utils.filterException(e)) {
          refresh();
        }
      }
    });
  }

  private void deleteAddRequest(final AddRequest addRequest) {
    new RefreshTask(ctx, this) {
      @Override
      protected void doInBack() throws Exception {
        addRequest.delete();
      }
    }.execute();
  }
}
