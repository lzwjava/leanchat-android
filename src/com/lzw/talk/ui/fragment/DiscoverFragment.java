package com.lzw.talk.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import com.lzw.talk.R;
import com.lzw.talk.adapter.NearPeopleAdapter;
import com.lzw.talk.avobject.User;
import com.lzw.talk.base.App;
import com.lzw.talk.service.UserService;
import com.lzw.talk.ui.activity.PersonInfoActivity;
import com.lzw.talk.ui.view.HeaderLayout;
import com.lzw.talk.ui.view.xlist.XListView;
import com.lzw.talk.util.ChatUtils;
import com.lzw.talk.util.NetAsyncTask;
import com.lzw.talk.util.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lzw on 14-9-17.
 */
public class DiscoverFragment extends BaseFragment
    implements AdapterView.OnItemClickListener,XListView.IXListViewListener{
  XListView listView;
  NearPeopleAdapter adapter;
  List<User> nears = new ArrayList<User>();

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    return inflater.inflate(R.layout.discover_fragment, null);
  }

  @Override
  public void onActivityCreated(Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    headerLayout.showTitle(R.string.discover);
    initXListView();
  }


  private void initXListView() {
    listView = (XListView) getView().findViewById(R.id.list_near);
    listView.setOnItemClickListener(this);
    listView.setPullLoadEnable(false);
    listView.setPullRefreshEnable(true);
    listView.setXListViewListener(this);
    adapter = new NearPeopleAdapter(ctx, nears);
    listView.setAdapter(adapter);

    onRefresh();
  }

  private void findNearbyPeople() {
    new NetAsyncTask(ctx, false) {
      List<User> users;

      @Override
      protected void doInBack() throws Exception {
        users = UserService.findNearbyPeople(adapter.getCount());
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
    User user = (User) adapter.getItem(position - 1);
    Intent intent = new Intent(ctx, PersonInfoActivity.class);
    intent.putExtra("from", "add");
    intent.putExtra("username", user.getUsername());
    startActivity(intent);
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
}
