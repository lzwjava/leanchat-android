package com.lzw.talk.adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.avos.avoscloud.AVUser;
import com.lzw.talk.R;
import com.lzw.talk.base.C;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lzw on 14-8-7.
 */
public class UserAdapter extends BaseAdapter {
  Activity cxt;
  List<AVUser> users = new ArrayList<AVUser>();

  public UserAdapter(Activity cxt) {
    this.cxt = cxt;
  }

  public void setUsers(List<AVUser> users) {
    this.users = users;
  }

  @Override
  public int getCount() {
    return users.size();
  }

  @Override
  public Object getItem(int position) {
    return users.get(position);
  }

  @Override
  public long getItemId(int position) {
    return position;
  }

  @Override
  public View getView(int position, View conView, ViewGroup parent) {
    if (conView == null) {
      LayoutInflater inflater = LayoutInflater.from(cxt);
      conView = inflater.inflate(R.layout.chat_user_row, null,false);
      TextView textView = (TextView) conView.findViewById(R.id.text);
      conView.setTag(textView);
    }
    TextView textView = (TextView) conView.getTag();
    AVUser user = users.get(position);
    textView.setText(user.getUsername());
    if(user.getBoolean(C.ONLINE)){
      textView.setTextColor(cxt.getResources().getColor(R.color.tree_green));
    }else{
      textView.setTextColor(cxt.getResources().getColor(R.color.black));
    }
    return conView;
  }
}
