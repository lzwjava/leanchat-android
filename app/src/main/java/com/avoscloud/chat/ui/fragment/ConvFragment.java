package com.avoscloud.chat.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import butterknife.ButterKnife;
import butterknife.InjectView;
import com.avoscloud.chat.R;
import com.avoscloud.chat.adapter.RoomAdapter;
import com.avoscloud.chat.entity.Room;
import com.avoscloud.chat.service.chat.ConvManager;
import com.avoscloud.chat.service.chat.IM;
import com.avoscloud.chat.service.event.MsgEvent;
import com.avoscloud.chat.ui.activity.ChatActivity;
import com.avoscloud.chat.ui.view.BaseListView;
import de.greenrobot.event.EventBus;

import java.util.List;

/**
 * Created by lzw on 14-9-17.
 */
public class ConvFragment extends BaseFragment implements IM.ConnectionListener {
  @InjectView(R.id.convList)
  BaseListView<Room> listView;
  @InjectView(R.id.im_client_state_view)
  View imClientStateView;
  private boolean hidden;
  private EventBus eventBus;
  private ConvManager convManager;
  private IM im;

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.message_fragment, container, false);
    ButterKnife.inject(this, view);
    return view;
  }

  @Override
  public void onActivityCreated(Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    eventBus = EventBus.getDefault();
    convManager = ConvManager.getInstance();
    im = IM.getInstance();
    im.setConnectionListener(this);
    initView();
    onRefresh();
    onConnectionChanged(im.isConnect());
  }

  private void onRefresh() {
    listView.onRefresh();
  }

  private void initView() {
    headerLayout.showTitle(R.string.messages);
    listView.init(new BaseListView.DataFactory<Room>() {
      @Override
      public List<Room> getDatas(int skip, int limit, List<Room> currentDatas) throws Exception {
        return convManager.findAndCacheRooms();
      }
    }, new RoomAdapter(getActivity()));

    listView.setItemListener(new BaseListView.ItemListener<Room>() {
      @Override
      public void onItemSelected(Room item) {
        ChatActivity.goByConv(getActivity(), item.getConv());
      }
    });
    listView.setToastIfEmpty(false);
    listView.setPullLoadEnable(false);
  }

  @Override
  public void onHiddenChanged(boolean hidden) {
    super.onHiddenChanged(hidden);
    this.hidden = hidden;
    if (!hidden) {
      listView.refreshWithoutAnim();
    }
  }


  @Override
  public void onResume() {
    super.onResume();
    if (!hidden) {
      listView.refreshWithoutAnim();
    }
    eventBus.register(this);
  }

  @Override
  public void onPause() {
    super.onPause();
    eventBus.unregister(this);
  }

  public void onEvent(MsgEvent event) {
    listView.refreshWithoutAnim();
  }

  @Override
  public void onConnectionChanged(boolean connect) {
    imClientStateView.setVisibility(connect ? View.GONE : View.VISIBLE);
  }
}
