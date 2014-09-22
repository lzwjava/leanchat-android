package com.lzw.talk.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.lzw.talk.R;
import com.lzw.talk.avobject.User;
import com.lzw.talk.base.App;
import com.lzw.talk.entity.Msg;
import com.lzw.talk.service.ChatService;
import com.lzw.talk.service.UserService;
import com.lzw.talk.util.PhotoUtil;
import com.lzw.talk.util.TimeUtils;
import com.lzw.talk.view.ViewHolder;
import com.lzw.talk.view.activity.ImageBrowerActivity;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ChatMsgAdapter extends BaseAdapter {
  int msgViewTypes = 4;
  ImageLoader imageLoader;

  public static interface MsgViewType {
    int COME_TEXT = 0;
    int TO_TEXT = 1;
    int COME_IMAGE = 2;
    int TO_IMAGE = 3;
  }

  private List<Msg> datas = new ArrayList<Msg>();

  private Context ctx;

  private LayoutInflater inflater;

  public ChatMsgAdapter(Context context) {
    ctx = context;
    inflater = LayoutInflater.from(context);
    imageLoader = ImageLoader.getInstance();
    ctx = context;
  }

  public void setDatas(List<Msg> datas) {
    this.datas = datas;
  }

  public int getCount() {
    return datas.size();
  }

  public Object getItem(int position) {
    return datas.get(position);
  }

  public long getItemId(int position) {
    return position;
  }

  public int getItemViewType(int position) {
    // TODO Auto-generated method stub
    Msg entity = datas.get(position);
    boolean comeMsg = entity.isComeMessage();
    if (entity.getType() == Msg.TYPE_TEXT) {
      if (comeMsg) {
        return MsgViewType.COME_TEXT;
      } else {
        return MsgViewType.TO_TEXT;
      }
    } else {
      if (comeMsg) {
        return MsgViewType.COME_IMAGE;
      } else {
        return MsgViewType.TO_IMAGE;
      }
    }
  }

  public int getViewTypeCount() {
    // TODO Auto-generated method stub
    return msgViewTypes;
  }

  public View getView(int position, View conView, ViewGroup parent) {
    Msg msg = datas.get(position);
    int itemViewType = getItemViewType(position);
    boolean isComMsg = msg.isComeMessage();
    if (conView == null) {
      conView = createViewByType(itemViewType);
    }
    TextView sendTimeView = ViewHolder.findViewById(conView, R.id.sendTimeView);
    TextView contentView = ViewHolder.findViewById(conView, R.id.textContent);
    TextView statusView = ViewHolder.findViewById(conView, R.id.status);
    ImageView imageView = ViewHolder.findViewById(conView, R.id.imageView);
    ImageView avatarView = ViewHolder.findViewById(conView, R.id.avatar);
    sendTimeView.setText(TimeUtils.millisecs2DateString(msg.getTimestamp()));
    String peerId = msg.getFromPeerId();
    User user = App.lookupUser(peerId);
    UserService.displayAvatar(user,avatarView);

    if (msg.getType() == Msg.TYPE_TEXT) {
      contentView.setText(msg.getContent());
    } else {
      displayImage(msg, imageView);
      setImageOnClickListener(msg.getContent(), imageView);
    }
    if (isComMsg == false) {
      statusView.setText(msg.getStatusDesc());
    }
    return conView;
  }

  private void setImageOnClickListener(final String uri, ImageView imageView) {
    imageView.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Intent intent = new Intent(ctx, ImageBrowerActivity.class);
        intent.putExtra("uri", uri);
        ctx.startActivity(intent);
      }
    });
  }

  public static void displayImage(Msg msg, ImageView imageView) {
    String content = msg.getContent();
    displayImageByUri(imageView, content);
  }

  public static void displayImageByUri(ImageView imageView, String uri) {
    String[] strings = uri.split("&");
    String localPath = strings[0];
    String url = strings[1];
    File file = new File(localPath);
    ImageLoader imageLoader = ImageLoader.getInstance();
    if (file.exists()) {
      //Logger.d("display from path "+localPath);
      imageLoader.displayImage("file://" + localPath, imageView);
    } else {
      //Logger.d("display from url");
      imageLoader.displayImage(url, imageView);
    }
  }

  public View createViewByType(int itemViewType) {
    View conView = null;
    switch (itemViewType) {
      case MsgViewType.COME_TEXT:
        conView = inflater.inflate(R.layout.chat_item_msg_text_left,
            null);
        break;
      case MsgViewType.TO_TEXT:
        conView = inflater.inflate(R.layout.chat_item_msg_text_right,
            null);
        break;
      case MsgViewType.COME_IMAGE:
        conView = inflater.inflate(R.layout.chat_item_msg_image_left, null);
        break;
      case MsgViewType.TO_IMAGE:
        conView = inflater.inflate(R.layout.chat_item_msg_image_right, null);
        break;
    }
    return conView;
  }
}
