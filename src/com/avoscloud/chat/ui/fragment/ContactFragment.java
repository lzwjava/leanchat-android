package com.avoscloud.chat.ui.fragment;

import android.app.AlertDialog;
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
import com.avoscloud.chat.R;
import com.avoscloud.chat.adapter.UserFriendAdapter;
import com.avoscloud.chat.avobject.User;
import com.avoscloud.chat.base.App;
import com.avoscloud.chat.service.AddRequestService;
import com.avoscloud.chat.service.CloudService;
import com.avoscloud.chat.service.UserService;
import com.avoscloud.chat.ui.activity.AddFriendActivity;
import com.avoscloud.chat.ui.activity.ChatActivity;
import com.avoscloud.chat.ui.activity.GroupListActivity;
import com.avoscloud.chat.ui.activity.NewFriendActivity;
import com.avoscloud.chat.ui.view.ClearEditText;
import com.avoscloud.chat.ui.view.EnLetterView;
import com.avoscloud.chat.ui.view.HeaderLayout;
import com.avoscloud.chat.util.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ContactFragment extends BaseFragment implements OnItemClickListener, OnItemLongClickListener, OnClickListener {
  ClearEditText clearEditText;
  TextView dialog;
  ListView friendsList;
  EnLetterView rightLetter;
  UserFriendAdapter userAdapter;
  List<User> friends = new ArrayList<User>();
  HeaderLayout headerLayout;
  ImageView msgTipsView;
  LinearLayout newFriendLayout, groupLayout;

  CharacterParser characterParser;
  PinyinComparator pinyinComparator;

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
    init();
    refresh();
  }

  private void init() {
    characterParser = CharacterParser.getInstance();
    pinyinComparator = new PinyinComparator();
    headerLayout = (HeaderLayout) getView().findViewById(R.id.headerLayout);
    headerLayout.showTitle(App.ctx.getString(R.string.contact));
    headerLayout.showRightImageButton(R.drawable.base_action_bar_add_bg_selector, new OnClickListener() {
      @Override
      public void onClick(View v) {
        Utils.goActivity(ctx, AddFriendActivity.class);
      }
    });
    initListView();
    initRightLetterView();
    initEditText();
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
    List<User> filterDateList = new ArrayList<User>();
    if (TextUtils.isEmpty(filterStr)) {
      filterDateList = friends;
    } else {
      filterDateList.clear();
      for (User sortModel : friends) {
        String name = sortModel.getUsername();
        if (name != null) {
          if (name.indexOf(filterStr.toString()) != -1
              || characterParser.getSelling(name).startsWith(
              filterStr.toString())) {
            filterDateList.add(sortModel);
          }
        }
      }
    }
    Collections.sort(filterDateList, pinyinComparator);
    userAdapter.updateListView(filterDateList);
  }

  private void fillFriendsData(List<User> datas) {
    friends.clear();
    int total = datas.size();
    for (int i = 0; i < total; i++) {
      User user = datas.get(i);
      User sortUser = new User();
      sortUser.setAvatar(user.getAvatar());
      sortUser.setUsername(user.getUsername());
      sortUser.setObjectId(user.getObjectId());
      //sortUser.(user.getContacts());
      String username = sortUser.getUsername();
      if (username != null) {
        String pinyin = characterParser.getSelling(sortUser.getUsername());
        String sortString = pinyin.substring(0, 1).toUpperCase();
        if (sortString.matches("[A-Z]")) {
          sortUser.setSortLetters(sortString.toUpperCase());
        } else {
          sortUser.setSortLetters("#");
        }
      } else {
        sortUser.setSortLetters("#");
      }
      friends.add(sortUser);
    }
    Collections.sort(friends, pinyinComparator);
  }

  private void initListView() {
    friendsList = (ListView) getView().findViewById(R.id.list_friends);
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
      //loadMsgsFromDB();
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
      Utils.goActivity(ctx, GroupListActivity.class);
    }
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

  private void setAddRequestTipsAndListView(boolean hasAddRequest, List<User> friends) {
    msgTipsView.setVisibility(hasAddRequest ? View.VISIBLE : View.GONE);
    fillFriendsData(friends);
    if (userAdapter == null) {
      userAdapter = new UserFriendAdapter(getActivity(), friends);
      friendsList.setAdapter(userAdapter);
    } else {
      userAdapter.notifyDataSetChanged();
    }
  }

  private boolean hidden;

  @Override
  public void onHiddenChanged(boolean hidden) {
    super.onHiddenChanged(hidden);
    this.hidden = hidden;
    if (!hidden) {
      refresh();
    }
  }

  @Override
  public void onResume() {
    super.onResume();
    if (!hidden) {
      refresh();
    }
  }

  public void refresh() {
    new SimpleNetTask(ctx, false) {
      boolean haveAddRequest;
      List<User> friends;

      @Override
      protected void doInBack() throws Exception {
        haveAddRequest = AddRequestService.hasAddRequest();
        friends = UserService.findFriends();
      }

      @Override
      public void onSucceed() {
        setAddRequestTipsAndListView(haveAddRequest, friends);
      }

    }.execute();
  }

  @Override
  public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
    // TODO Auto-generated method stub
    User user = (User) userAdapter.getItem(position - 1);
    ChatActivity.goUserChat(getActivity(),user.getObjectId());
  }

  @Override
  public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int position,
                                 long arg3) {
    // TODO Auto-generated method stub
    User user = (User) userAdapter.getItem(position - 1);
    showDeleteDialog(user);
    return true;
  }

  public void showDeleteDialog(final User user) {
    new AlertDialog.Builder(ctx).setMessage(R.string.deleteContact)
        .setPositiveButton(R.string.sure, new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            deleteFriend(user);
          }
        }).setNegativeButton(R.string.cancel, null).show();
  }

  private void deleteFriend(final User user) {
    new SimpleNetTask(ctx) {
      @Override
      protected void doInBack() throws Exception {
        User curUser = User.curUser();
        CloudService.removeFriendForBoth(curUser, user);
      }

      @Override
      public void onSucceed() {
        Utils.toast(App.ctx.getString(R.string.deleteSucceed));
        userAdapter.remove(user);
      }
    }.execute();
  }
}
