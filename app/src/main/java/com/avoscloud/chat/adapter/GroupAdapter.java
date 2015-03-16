package com.avoscloud.chat.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.avos.avoscloud.im.v2.AVIMConversation;
import com.avoscloud.chat.R;
import com.avoscloud.chat.service.chat.ConvManager;
import com.avoscloud.chat.ui.view.ViewHolder;

import java.util.List;

/**
 * Created by lzw on 14-10-8.
 */
public class GroupAdapter extends BaseListAdapter<AVIMConversation> {
  public GroupAdapter(Context ctx, List<AVIMConversation> datas) {
    super(ctx, datas);
  }

  @Override
  public View getView(int position, View conView, ViewGroup parent) {
    if (conView == null) {
      //conView = View.inflate(ctx, R.layout.group_item,null);
      conView = inflater.inflate(R.layout.group_item, null);
    }
    TextView nameView = ViewHolder.findViewById(conView, R.id.name);
    AVIMConversation conv = datas.get(position);
    nameView.setText(ConvManager.titleOfConv(conv));
    return conView;
  }
}
