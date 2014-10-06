package com.lzw.talk.ui.fragment;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
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
import com.avos.avoscloud.SaveCallback;
import com.lzw.talk.R;
import com.lzw.talk.adapter.UserFriendAdapter;
import com.lzw.talk.avobject.User;
import com.lzw.talk.base.App;
import com.lzw.talk.service.AddRequestService;
import com.lzw.talk.service.CloudService;
import com.lzw.talk.service.UserService;
import com.lzw.talk.ui.activity.AddFriendActivity;
import com.lzw.talk.ui.activity.NewFriendActivity;
import com.lzw.talk.ui.activity.PersonInfoActivity;
import com.lzw.talk.ui.view.ClearEditText;
import com.lzw.talk.ui.view.EnLetterView;
import com.lzw.talk.ui.view.HeaderLayout;
import com.lzw.talk.ui.view.dialog.DialogTips;
import com.lzw.talk.util.CharacterParser;
import com.lzw.talk.util.PinyinComparator;
import com.lzw.talk.util.SimpleNetTask;
import com.lzw.talk.util.Utils;

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
  LinearLayout newFriendLayout;

  CharacterParser characterParser;
  PinyinComparator pinyinComparator;

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    // TODO Auto-generated method stub
    return inflater.inflate(R.layout.fragment_contacts, container, false);
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
    clearEditText.addTextChangedListener(new TextWatcher() {

      @Override
      public void onTextChanged(CharSequence s, int start, int before,
                                int count) {
        filterData(s.toString());
      }

      @Override
      public void beforeTextChanged(CharSequence s, int start, int count,
                                    int after) {

      }

      @Override
      public void afterTextChanged(Editable s) {

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

  private void filledData(List<User> datas) {
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
    friendsList = (ListView) ctx.findViewById(R.id.list_friends);
    LayoutInflater mInflater = LayoutInflater.from(ctx);
    RelativeLayout headView = (RelativeLayout) mInflater.inflate(R.layout.include_new_friend, null);
    msgTipsView = (ImageView) headView.findViewById(R.id.iv_msg_tips);
    newFriendLayout = (LinearLayout) headView.findViewById(R.id.layout_new);
    newFriendLayout.setOnClickListener(this);

    friendsList.addHeaderView(headView);
    userAdapter = new UserFriendAdapter(getActivity(), friends);
    friendsList.setAdapter(userAdapter);
    friendsList.setOnItemClickListener(this);
    friendsList.setOnItemLongClickListener(this);
    friendsList.setOnTouchListener(new OnTouchListener() {

      @Override
      public boolean onTouch(View v, MotionEvent event) {
        Utils.hideSoftInputView(ctx);
        return false;
      }
    });
  }

  @Override
  public void setUserVisibleHint(boolean isVisibleToUser) {
    // TODO Auto-generated method stub
    if (isVisibleToUser) {
      //refresh();
    }
    super.setUserVisibleHint(isVisibleToUser);
  }

  private void initRightLetterView() {
    rightLetter = (EnLetterView) ctx.findViewById(R.id.right_letter);
    dialog = (TextView) ctx.findViewById(R.id.dialog);
    rightLetter.setTextView(dialog);
    rightLetter.setOnTouchingLetterChangedListener(new LetterListViewListener());
  }

  @Override
  public void onClick(View v) {
    int viewId = v.getId();
    if (viewId == R.id.layout_new) {
      Utils.goActivity(ctx, NewFriendActivity.class);
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
    if (hasAddRequest) {
      msgTipsView.setVisibility(View.VISIBLE);
    } else {
      msgTipsView.setVisibility(View.GONE);
    }
    filledData(friends);
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
        App app = App.getInstance();
        app.setFriends(friends);
        setAddRequestTipsAndListView(haveAddRequest, friends);
      }

    }.execute();
  }

  @Override
  public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
    // TODO Auto-generated method stub
    User user = (User) userAdapter.getItem(position - 1);
    Intent intent = new Intent(getActivity(), PersonInfoActivity.class);
    intent.putExtra("from", "other");
    intent.putExtra("username", user.getUsername());
    startActivity(intent);
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
    DialogTips dialog = new DialogTips(getActivity(), user.getUsername(),
        App.ctx.getString(R.string.deleteContact),
        App.ctx.getString(R.string.sure), true, true);
    dialog.SetOnSuccessListener(new DialogInterface.OnClickListener() {
      public void onClick(DialogInterface dialogInterface, int userId) {
        deleteFriend(user);
      }
    });
    dialog.show();
    dialog = null;
  }

  private void deleteFriend(final User user) {
    new SimpleNetTask(ctx) {
      @Override
      public void onSucceed() {
        Utils.toast(App.ctx.getString(R.string.deleteSucceed));
        userAdapter.remove(user);
      }

      @Override
      protected void doInBack() throws Exception {
        User curUser = User.curUser();
        CloudService.removeFriendForBoth(curUser, user);
      }
    }.execute();
  }
}
