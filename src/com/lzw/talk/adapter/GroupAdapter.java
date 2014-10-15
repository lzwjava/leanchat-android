package com.lzw.talk.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.lzw.talk.R;
import com.lzw.talk.avobject.ChatGroup;
import com.lzw.talk.ui.view.ViewHolder;

import java.util.List;

/**
 * Created by lzw on 14-10-8.
 */
public class GroupAdapter extends BaseListAdapter<ChatGroup> {
  public GroupAdapter(Context ctx, List<ChatGroup> datas) {
    super(ctx, datas);
  }

  @Override
  public View getView(int position, View conView, ViewGroup parent) {
    if (conView == null) {
      //conView = View.inflate(ctx, R.layout.group_item,null);
      conView = inflater.inflate(R.layout.group_item, null, false);
    }
    TextView nameView = ViewHolder.findViewById(conView, R.id.name);
    ChatGroup chatGroup = datas.get(position);
    nameView.setText(chatGroup.getTitle());
    return conView;
  }
}
