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
import com.lzw.talk.ui.activity.ImageBrowerActivity;
import com.lzw.talk.ui.activity.LocationActivity;
import com.lzw.talk.ui.view.PlayButton;
import com.lzw.talk.ui.view.ViewHolder;
import com.lzw.talk.util.TimeUtils;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ChatMsgAdapter extends BaseAdapter {
  ImageLoader imageLoader;

  int msgViewTypes = 8;

  public static interface MsgViewType {
    int COME_TEXT = 0;
    int TO_TEXT = 1;
    int COME_IMAGE = 2;
    int TO_IMAGE = 3;
    int COME_AUDIO = 4;
    int TO_AUDIO = 5;
    int COME_LOCATION = 6;
    int TO_LOCATION = 7;
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
    int type = entity.getType();
    if (type == Msg.TYPE_TEXT) {
      return comeMsg ? MsgViewType.COME_TEXT : MsgViewType.TO_TEXT;
    } else if (type == Msg.TYPE_IMAGE) {
      return comeMsg ? MsgViewType.COME_IMAGE : MsgViewType.TO_IMAGE;
    } else if (type == Msg.TYPE_AUDIO) {
      return comeMsg ? MsgViewType.COME_AUDIO : MsgViewType.TO_AUDIO;
    } else {
      assert type == Msg.TYPE_LOCATION;
      return comeMsg ? MsgViewType.COME_LOCATION : MsgViewType.TO_LOCATION;
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
    PlayButton playBtn = ViewHolder.findViewById(conView, R.id.playBtn);
    TextView locationView = ViewHolder.findViewById(conView, R.id.locationView);

    sendTimeView.setText(TimeUtils.millisecs2DateString(msg.getTimestamp()));
    String peerId = msg.getFromPeerId();
    User user = App.lookupUser(peerId);
    UserService.displayAvatar(user, avatarView);

    int type = msg.getType();
    if (type == Msg.TYPE_TEXT) {
      contentView.setText(msg.getContent());
    } else if (type == Msg.TYPE_IMAGE) {
      displayImage(msg, imageView);
      setImageOnClickListener(msg.getContent(), imageView);
    } else if (type == Msg.TYPE_AUDIO) {
      initPlayBtn(msg, playBtn);
    } else if (type == Msg.TYPE_LOCATION) {
      setLocationView(msg, locationView);
    }
    if (isComMsg == false) {
      statusView.setText(msg.getStatusDesc());
    }
    return conView;
  }

  public void setLocationView(Msg msg, TextView locationView) {
    try {
      String content = msg.getContent();
      if (content != null && !content.equals("")) {
        String address = content.split("&")[0];
        final String latitude = content.split("&")[1];//维度
        final String longtitude = content.split("&")[2];//经度
        locationView.setText(address);
        locationView.setOnClickListener(new View.OnClickListener() {

          @Override
          public void onClick(View arg0) {
            // TODO Auto-generated method stub
            Intent intent = new Intent(ctx, LocationActivity.class);
            intent.putExtra("type", "scan");
            intent.putExtra("latitude", Double.parseDouble(latitude));//维度
            intent.putExtra("longtitude", Double.parseDouble(longtitude));//经度
            ctx.startActivity(intent);
          }
        });
      }
    } catch (Exception e) {
    }
  }

  private void initPlayBtn(Msg msg, PlayButton playBtn) {
    playBtn.setPath(msg.getAudioPath());
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
    HashMap<String, String> parts = ChatService.parseUri(uri);
    String localPath = parts.get("path");
    String url = parts.get("url");
    File file = new File(localPath);
    ImageLoader imageLoader = ImageLoader.getInstance();
    if (file.exists()) {
      imageLoader.displayImage("file://" + localPath, imageView);
    } else {
      imageLoader.displayImage(url, imageView);
    }
  }

  public View createViewByType(int itemViewType) {
    int[] types = new int[]{MsgViewType.COME_TEXT, MsgViewType.TO_TEXT,
        MsgViewType.COME_IMAGE, MsgViewType.TO_IMAGE, MsgViewType.COME_AUDIO,
        MsgViewType.TO_AUDIO, MsgViewType.COME_LOCATION, MsgViewType.TO_LOCATION};
    int[] layoutIds = new int[]{
        R.layout.chat_item_msg_text_left,
        R.layout.chat_item_msg_text_left,
        R.layout.chat_item_msg_image_left,
        R.layout.chat_item_msg_image_right,
        R.layout.chat_item_msg_audio_left,
        R.layout.chat_item_msg_audio_right,
        R.layout.chat_item_msg_location_left,
        R.layout.chat_item_msg_location_right
    };
    int i;
    for (i = 0; i < types.length; i++) {
      if (itemViewType == types[i]) {
        break;
      }
    }
    return inflater.inflate(layoutIds[i], null);
  }
}
