package com.avoscloud.chat.ui.contact;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SectionIndexer;
import android.widget.TextView;
import com.avoscloud.chat.R;
import com.avoscloud.chat.entity.SortUser;
import com.avoscloud.chat.entity.avobject.User;
import com.avoscloud.chat.service.UserService;
import com.avoscloud.chat.ui.view.BaseListAdapter;
import com.avoscloud.leanchatlib.view.ViewHolder;

@SuppressLint("DefaultLocale")
public class ContactFragmentAdapter extends BaseListAdapter<SortUser> implements SectionIndexer {

  public ContactFragmentAdapter(Context ctx) {
    super(ctx);
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    if (convertView == null) {
      convertView = LayoutInflater.from(ctx).inflate(
          R.layout.common_user_item, null);
    }
    TextView alpha = ViewHolder.findViewById(convertView, R.id.alpha);
    TextView nameView = ViewHolder.findViewById(convertView, R.id.tv_friend_name);
    ImageView avatarView = ViewHolder.findViewById(convertView, R.id.img_friend_avatar);

    SortUser friend = datas.get(position);
    final String name = friend.getInnerUser().getUsername();

    UserService.displayAvatar(friend.getInnerUser(), avatarView);
    nameView.setText(name);

    int section = getSectionForPosition(position);
    if (position == getPositionForSection(section)) {
      alpha.setVisibility(View.VISIBLE);
      alpha.setText(friend.getSortLetters());
    } else {
      alpha.setVisibility(View.GONE);
    }

    return convertView;
  }

  public int getSectionForPosition(int position) {
    return datas.get(position).getSortLetters().charAt(0);
  }


  @SuppressLint("DefaultLocale")
  public int getPositionForSection(int section) {
    for (int i = 0; i < getCount(); i++) {
      String sortStr = datas.get(i).getSortLetters();
      char firstChar = sortStr.toUpperCase().charAt(0);
      if (firstChar == section) {
        return i;
      }
    }
    return -1;
  }

  @Override
  public Object[] getSections() {
    return null;
  }

}
