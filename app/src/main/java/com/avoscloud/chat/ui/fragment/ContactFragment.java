package com.avoscloud.chat.ui.fragment;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.SaveCallback;
import com.avoscloud.chat.R;
import com.avoscloud.chat.adapter.UserFriendAdapter;
import com.avoscloud.chat.base.App;
import com.avoscloud.chat.entity.SortUser;
import com.avoscloud.chat.service.AddRequestService;
import com.avoscloud.chat.service.CacheService;
import com.avoscloud.chat.service.UserService;
import com.avoscloud.chat.service.chat.IM;
import com.avoscloud.chat.ui.activity.AddFriendActivity;
import com.avoscloud.chat.ui.activity.ChatActivity;
import com.avoscloud.chat.ui.activity.GroupConvListActivity;
import com.avoscloud.chat.ui.activity.NewFriendActivity;
import com.avoscloud.chat.ui.view.ClearEditText;
import com.avoscloud.chat.ui.view.EnLetterView;
import com.avoscloud.chat.ui.view.HeaderLayout;
import com.avoscloud.chat.ui.view.xlist.XListView;
import com.avoscloud.chat.util.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ContactFragment extends BaseFragment implements OnItemClickListener, OnItemLongClickListener, OnClickListener, XListView.IXListViewListener {
  private static CharacterParser characterParser;
  private static PinyinComparator pinyinComparator;
  private ClearEditText clearEditText;
  private TextView dialog;
  private XListView friendsList;
  private EnLetterView rightLetter;
  private UserFriendAdapter userAdapter;
  private List<SortUser> friends = new ArrayList<SortUser>();
  private HeaderLayout headerLayout;
  private ImageView msgTipsView;
  private LinearLayout newFriendLayout, groupLayout;
  private IM im;
  private boolean hidden;

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    // TODO Auto-generated method stub
    return inflater.inflate(R.layout.contact_fragment, container, false);
  }

  @Override
  public void onActivityCreated(Bundle savedInstanceState) {
    // TODO Auto-generated method stub
    super.onActivityCreated(savedInstanceState);
    characterParser = CharacterParser.getInstance();
    pinyinComparator = new PinyinComparator();
    im = IM.getInstance();
    initHeader();
    initListView();
    initRightLetterView();
    initEditText();
    onRefresh();
  }

  private void initHeader() {
    headerLayout = (HeaderLayout) getView().findViewById(R.id.headerLayout);
    headerLayout.showTitle(App.ctx.getString(R.string.contact));
    headerLayout.showRightImageButton(R.drawable.base_action_bar_add_bg_selector, new OnClickListener() {
      @Override
      public void onClick(View v) {
        Utils.goActivity(ctx, AddFriendActivity.class);
      }
    });
  }

  private void initEditText() {
    clearEditText = (ClearEditText) getView().findViewById(R.id.et_msg_search);
    clearEditText.addTextChangedListener(new SimpleTextWatcher() {
      @Override
      public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
        filterData(charSequence.toString());
      }
    });
  }

  private void filterData(String filterStr) {
    if (TextUtils.isEmpty(filterStr)) {
      userAdapter.updateDatas(friends);
    } else {
      List<SortUser> filterDateList = new ArrayList<SortUser>();
      filterDateList.clear();
      for (SortUser sortModel : friends) {
        String name = sortModel.getInnerUser().getUsername();
        if (name != null && (name.contains(filterStr)
            || characterParser.getSelling(name).startsWith(
            filterStr))) {
          filterDateList.add(sortModel);
        }
      }
      Collections.sort(filterDateList, pinyinComparator);
      userAdapter.updateDatas(filterDateList);
    }
  }

  private List<SortUser> convertAVUser(List<AVUser> datas) {
    List<SortUser> sortUsers = new ArrayList<SortUser>();
    int total = datas.size();
    for (int i = 0; i < total; i++) {
      AVUser avUser = datas.get(i);
      SortUser sortUser = new SortUser();
      sortUser.setInnerUser(avUser);
      String username = avUser.getUsername();
      if (username != null) {
        String pinyin = characterParser.getSelling(username);
        String sortString = pinyin.substring(0, 1).toUpperCase();
        if (sortString.matches("[A-Z]")) {
          sortUser.setSortLetters(sortString.toUpperCase());
        } else {
          sortUser.setSortLetters("#");
        }
      } else {
        sortUser.setSortLetters("#");
      }
      sortUsers.add(sortUser);
    }
    Collections.sort(sortUsers, pinyinComparator);
    return sortUsers;
  }

  private void initListView() {
    friendsList = (XListView) getView().findViewById(R.id.list_friends);
    friendsList.setPullRefreshEnable(true);
    friendsList.setPullLoadEnable(false);
    friendsList.setXListViewListener(this);
    LayoutInflater mInflater = LayoutInflater.from(ctx);
    RelativeLayout headView = (RelativeLayout) mInflater.inflate(R.layout.contact_include_new_friend, null);
    msgTipsView = (ImageView) headView.findViewById(R.id.iv_msg_tips);
    newFriendLayout = (LinearLayout) headView.findViewById(R.id.layout_new);
    groupLayout = (LinearLayout) headView.findViewById(R.id.layout_group);

    newFriendLayout.setOnClickListener(this);
    groupLayout.setOnClickListener(this);

    friendsList.addHeaderView(headView);
    userAdapter = new UserFriendAdapter(getActivity(), friends);
    friendsList.setAdapter(userAdapter);
    friendsList.setOnItemClickListener(this);
    friendsList.setOnItemLongClickListener(this);
    friendsList.setOnTouchListener(new OnTouchListener() {

      @Override
      public boolean onTouch(View v, MotionEvent event) {
        Utils.hideSoftInputView(getActivity());
        return false;
      }
    });
  }

  @Override
  public void setUserVisibleHint(boolean isVisibleToUser) {
    // TODO Auto-generated method stub
    if (isVisibleToUser) {
      //refreshMsgsFromDB();
    }
    super.setUserVisibleHint(isVisibleToUser);
  }

  private void initRightLetterView() {
    rightLetter = (EnLetterView) getView().findViewById(R.id.right_letter);
    dialog = (TextView) getView().findViewById(R.id.dialog);
    rightLetter.setTextView(dialog);
    rightLetter.setOnTouchingLetterChangedListener(new LetterListViewListener());
  }

  @Override
  public void onClick(View v) {
    int viewId = v.getId();
    if (viewId == R.id.layout_new) {
      Utils.goActivity(ctx, NewFriendActivity.class);
    } else if (viewId == R.id.layout_group) {
      Utils.goActivity(ctx, GroupConvListActivity.class);
    }
  }

  @Override
  public void onRefresh() {
    new NetAsyncTask(ctx, false) {
      boolean haveAddRequest;
      List<AVUser> friends1;

      @Override
      protected void doInBack() throws Exception {
        haveAddRequest = AddRequestService.hasAddRequest();
        friends1 = UserService.findFriends();
        CacheService.registerUsers(friends1);
        CacheService.setFriendIds(AVOSUtils.getObjectIds(friends1));
      }

      @Override
      protected void onPost(Exception e) {
        friendsList.stopRefresh();
        if (e != null) {
          Utils.toast(ctx, e.getMessage());
        } else {
          msgTipsView.setVisibility(haveAddRequest ? View.VISIBLE : View.GONE);
          List<SortUser> sortUsers = convertAVUser(friends1);
          ContactFragment.this.friends = Collections.unmodifiableList(sortUsers);
          userAdapter.updateDatas(sortUsers);
        }
      }
    }.execute();
  }

  @Override
  public void onLoadMore() {
  }

  @Override
  public void onHiddenChanged(boolean hidden) {
    super.onHiddenChanged(hidden);
    this.hidden = hidden;
    if (!hidden) {
      onRefresh();
    }
  }

  @Override
  public void onResume() {
    super.onResume();
    if (!hidden) {
      onRefresh();
    }
  }

  @Override
  public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
    // TODO Auto-generated method stub
    SortUser user = (SortUser) arg0.getAdapter().getItem(position);
    ChatActivity.goByUserId(getActivity(), user.getInnerUser().getObjectId());
  }

  @Override
  public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int position,
                                 long arg3) {
    // TODO Auto-generated method stub
    SortUser user = (SortUser) arg0.getAdapter().getItem(position);
    showDeleteDialog(user);
    return true;
  }

  public void showDeleteDialog(final SortUser user) {
    new AlertDialog.Builder(ctx).setMessage(R.string.deleteContact)
        .setPositiveButton(R.string.sure, new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            deleteFriend(user);
          }
        }).setNegativeButton(R.string.cancel, null).show();
  }

  private void deleteFriend(final SortUser user) {
    final ProgressDialog dialog = Utils.showSpinnerDialog(getActivity());
    UserService.removeFriend(user.getInnerUser().getObjectId(), new SaveCallback() {
      @Override
      public void done(AVException e) {
        dialog.dismiss();
        if (Utils.filterException(e)) {
          onRefresh();
        }
      }
    });
  }

  private class LetterListViewListener implements
      EnLetterView.OnTouchingLetterChangedListener {

    @Override
    public void onTouchingLetterChanged(String s) {
      int position = userAdapter.getPositionForSection(s.charAt(0));
      if (position != -1) {
        friendsList.setSelection(position);
      }
    }
  }
}
