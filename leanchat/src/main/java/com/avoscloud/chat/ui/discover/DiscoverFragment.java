package com.avoscloud.chat.ui.discover;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import butterknife.InjectView;
import com.avos.avoscloud.AVUser;
import com.avoscloud.chat.R;
import com.avoscloud.chat.service.PreferenceMap;
import com.avoscloud.chat.service.UserService;
import com.avoscloud.chat.ui.base_activity.BaseFragment;
import com.avoscloud.chat.ui.contact.ContactPersonInfoActivity;
import com.avoscloud.chat.ui.view.BaseListView;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.PauseOnScrollListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lzw on 14-9-17.
 */
public class DiscoverFragment extends BaseFragment {

  private final SortDialogListener distanceListener = new SortDialogListener(UserService.ORDER_DISTANCE);
  private final SortDialogListener updatedAtListener = new SortDialogListener(UserService.ORDER_UPDATED_AT);
  @InjectView(R.id.list_near)
  BaseListView<AVUser> listView;
  DiscoverFragmentUserAdapter adapter;
  List<AVUser> nears = new ArrayList<AVUser>();
  int orderType;
  PreferenceMap preferenceMap;

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    return inflater.inflate(R.layout.discover_fragment, container, false);
  }

  @Override
  public void onActivityCreated(Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    preferenceMap = PreferenceMap.getCurUserPrefDao(getActivity());
    orderType = preferenceMap.getNearbyOrder();
    headerLayout.showTitle(R.string.discover_title);
    headerLayout.showRightImageButton(R.drawable.nearby_order, new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.discover_fragment_sort).setPositiveButton(R.string.discover_fragment_loginTime,
            updatedAtListener).setNegativeButton(R.string.discover_fragment_distance, distanceListener).show();
      }
    });
    initXListView();
    listView.onRefresh();
  }

  private void initXListView() {
    adapter = new DiscoverFragmentUserAdapter(ctx, nears);
    listView = (BaseListView<AVUser>) getView().findViewById(R.id.list_near);
    listView.init(new BaseListView.DataFactory<AVUser>() {
      @Override
      public List<AVUser> getDatasInBackground(int skip, int limit, List<AVUser> currentDatas) throws Exception {
        return UserService.findNearbyPeople(orderType, skip, limit);
      }
    }, adapter);

    listView.setItemListener(new BaseListView.ItemListener<AVUser>() {
      @Override
      public void onItemSelected(AVUser item) {
        ContactPersonInfoActivity.goPersonInfo(ctx, item.getObjectId());
      }
    });

    PauseOnScrollListener listener = new PauseOnScrollListener(ImageLoader.getInstance(),
        true, true);
    listView.setOnScrollListener(listener);
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    preferenceMap.setNearbyOrder(orderType);
  }

  public class SortDialogListener implements DialogInterface.OnClickListener {
    int orderType;

    public SortDialogListener(int orderType) {
      this.orderType = orderType;
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
      DiscoverFragment.this.orderType = orderType;
      listView.onRefresh();
    }
  }
}
