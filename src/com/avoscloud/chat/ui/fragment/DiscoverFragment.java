package com.avoscloud.chat.ui.fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import com.avoscloud.chat.R;
import com.avoscloud.chat.adapter.NearPeopleAdapter;
import com.avoscloud.chat.avobject.User;
import com.avoscloud.chat.service.PreferenceMap;
import com.avoscloud.chat.service.UserService;
import com.avoscloud.chat.ui.activity.PersonInfoActivity;
import com.avoscloud.chat.ui.view.xlist.XListView;
import com.avoscloud.chat.util.ChatUtils;
import com.avoscloud.chat.util.NetAsyncTask;
import com.avoscloud.chat.util.Utils;
import com.nostra13.universalimageloader.core.listener.PauseOnScrollListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lzw on 14-9-17.
 */
public class DiscoverFragment extends BaseFragment
    implements AdapterView.OnItemClickListener, XListView.IXListViewListener {
  XListView listView;
  NearPeopleAdapter adapter;
  List<User> nears = new ArrayList<User>();
  int orderType;
  PreferenceMap preferenceMap;

  private final SortDialogListener distanceListener = new SortDialogListener(UserService.ORDER_DISTANCE);
  private final SortDialogListener updatedAtListener = new SortDialogListener(UserService.ORDER_UPDATED_AT);

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    return inflater.inflate(R.layout.discover_fragment, container, false);
  }

  @Override
  public void onActivityCreated(Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    preferenceMap = PreferenceMap.getCurUserPrefDao(getActivity());
    orderType = preferenceMap.getNearbyOrder();
    headerLayout.showTitle(R.string.discover);
    headerLayout.showRightImageButton(R.drawable.nearby_order, new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.sort).setPositiveButton(R.string.loginTime,
            updatedAtListener).setNegativeButton(R.string.distance, distanceListener).show();
      }
    });
    initXListView();
  }

  public class SortDialogListener implements DialogInterface.OnClickListener {
    int orderType;

    public SortDialogListener(int orderType) {
      this.orderType = orderType;
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
      DiscoverFragment.this.orderType = orderType;
      onRefresh();
    }
  }


  private void initXListView() {
    listView = (XListView) getView().findViewById(R.id.list_near);
    listView.setOnItemClickListener(this);
    listView.setPullLoadEnable(true);
    listView.setPullRefreshEnable(true);
    listView.setXListViewListener(this);
    adapter = new NearPeopleAdapter(ctx, nears);
    listView.setAdapter(adapter);
    PauseOnScrollListener listener = new PauseOnScrollListener(UserService.imageLoader,
        true, true);
    listView.setOnScrollListener(listener);
    onRefresh();
  }

  private void findNearbyPeople() {
    new NetAsyncTask(ctx, false) {
      List<User> users;

      @Override
      protected void doInBack() throws Exception {
        users = UserService.findNearbyPeople(adapter.getCount(), orderType);
      }

      @Override
      protected void onPost(Exception e) {
        stopLoadMore();
        stopRefresh();
        if (e != null) {
          e.printStackTrace();
          Utils.toastCheckNetwork(ctx);
        } else {
          ChatUtils.handleListResult(listView, adapter, users);
        }
      }
    }.execute();
  }

  @Override
  public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
    // TODO Auto-generated method stub
    User user = (User) arg0.getAdapter().getItem(position);
    PersonInfoActivity.goPersonInfo(ctx, user.getObjectId());
  }

  @Override
  public void onRefresh() {
    // TODO Auto-generated method stub
    adapter.clear();
    findNearbyPeople();
  }

  private void stopLoadMore() {
    if (listView.getPullLoading()) {
      listView.stopLoadMore();
    }
  }

  private void stopRefresh() {
    if (listView.getPullRefreshing()) {
      listView.stopRefresh();
    }
  }

  @Override
  public void onLoadMore() {
    findNearbyPeople();
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    preferenceMap.setNearbyOrder(orderType);
  }
}
