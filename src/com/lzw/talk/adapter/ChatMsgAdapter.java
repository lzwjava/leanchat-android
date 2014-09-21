package com.lzw.talk.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.lzw.talk.R;
import com.lzw.talk.entity.Msg;
import com.lzw.talk.util.Logger;
import com.lzw.talk.util.PhotoUtil;
import com.lzw.talk.util.TimeUtils;
import com.lzw.talk.view.ViewHolder;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageSize;
import com.nostra13.universalimageloader.utils.ImageSizeUtils;

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
    TextView sendTimeView= ViewHolder.findViewById(conView, R.id.sendTimeView);
    TextView usernameView = ViewHolder.findViewById(conView, R.id.username);
    TextView contentView = ViewHolder.findViewById(conView,R.id.textContent);
    TextView statusView = ViewHolder.findViewById(conView,R.id.status);
    ImageView imageView = ViewHolder.findViewById(conView,R.id.imageView);
    //viewHolder.sendTimeView.setText(msg.getTimestamp() + "");
    sendTimeView.setText(TimeUtils.millisecs2DateString(msg.getTimestamp()));
    usernameView.setText(msg.getFromName());
    if (msg.getType() == Msg.TYPE_TEXT) {
      contentView.setText(msg.getContent());
    } else {
      displayImage(msg,imageView);
    }
    if (isComMsg == false) {
      statusView.setText(msg.getStatusDesc());
    }
    return conView;
  }

  private void displayImage(Msg msg, ImageView imageView) {
    String content=msg.getContent();
    String[] strings = content.split("&");
    String localPath=strings[0];
    String url=strings[1];
    File file=new File(localPath);
    if(msg.isComeMessage()==false && file.exists()){
      //Logger.d("display from path "+localPath);
      imageLoader.displayImage("file://"+localPath, imageView);
    }else{
      //Logger.d("display from url");
      imageLoader.displayImage(url,imageView);
    }
  }

  public View createViewByType(int itemViewType) {
    View conView=null;
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
