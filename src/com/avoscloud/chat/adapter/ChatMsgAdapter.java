package com.avoscloud.chat.adapter;

import android.content.Intent;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.avoscloud.chat.R;
import com.avoscloud.chat.avobject.User;
import com.avoscloud.chat.entity.RoomType;
import com.avoscloud.chat.service.AudioHelper;
import com.avoscloud.chat.util.EmotionUtils;
import com.avoscloud.chat.ui.activity.ChatActivity;
import com.avoscloud.chat.ui.activity.LocationActivity;
import com.avoscloud.chat.ui.view.PlayButton;
import com.avoscloud.chat.ui.view.ViewHolder;
import com.avoscloud.chat.base.App;
import com.avoscloud.chat.entity.Msg;
import com.avoscloud.chat.service.UserService;
import com.avoscloud.chat.ui.activity.ImageBrowerActivity;
import com.avoscloud.chat.util.PathUtils;
import com.avoscloud.chat.util.PhotoUtil;
import com.avoscloud.chat.util.TimeUtils;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.io.File;
import java.util.List;

public class ChatMsgAdapter extends BaseListAdapter<Msg> {
  int msgViewTypes = 8;

  public enum MsgViewType {
    ComeText(0), ToText(1), ComeImage(2), ToImage(3), ComeAudio(4), ToAudio(5), ComeLocation(6), ToLocation(7);
    int value;

    MsgViewType(int value) {
      this.value = value;
    }

    public int getValue() {
      return value;
    }
  }

  ChatActivity chatActivity;

  public ChatMsgAdapter(ChatActivity chatActivity, List<Msg> datas) {
    super(chatActivity, datas);
    this.chatActivity = chatActivity;
  }

  public int getItemPosById(String objectId) {
    for (int i = 0; i < getCount(); i++) {
      Msg itemMsg = datas.get(i);
      if (itemMsg.getObjectId().equals(objectId)) {
        return i;
      }
    }
    return -1;
  }

  public Msg getItem(String objectId) {
    for (Msg msg : datas) {
      if (msg.getObjectId().equals(objectId)) {
        return msg;
      }
    }
    return null;
  }

  @Override
  public int getItemViewType(int position) {
    Msg entity = datas.get(position);
    boolean comeMsg = entity.isComeMessage();
    Msg.Type type = entity.getType();
    MsgViewType viewType;
    switch (type) {
      case Text:
        viewType = comeMsg ? MsgViewType.ComeText : MsgViewType.ToText;
        break;
      case Image:
        viewType = comeMsg ? MsgViewType.ComeImage : MsgViewType.ToImage;
        break;
      case Audio:
        viewType = comeMsg ? MsgViewType.ComeAudio : MsgViewType.ToAudio;
        break;
      case Location:
        viewType = comeMsg ? MsgViewType.ComeLocation : MsgViewType.ToLocation;
        break;
      default:
        throw new IllegalStateException();
    }
    return viewType.getValue();
  }

  public int getViewTypeCount() {
    return msgViewTypes;
  }

  public View getView(int position, View conView, ViewGroup parent) {
    Msg msg = datas.get(position);
    int itemViewType = getItemViewType(position);
    boolean isComMsg = msg.isComeMessage();
    if (conView == null) {
      conView = createViewByType(itemViewType, msg.getType(), isComMsg);
    }
    TextView sendTimeView = ViewHolder.findViewById(conView, R.id.sendTimeView);
    TextView contentView = ViewHolder.findViewById(conView, R.id.textContent);
    ImageView imageView = ViewHolder.findViewById(conView, R.id.imageView);
    ImageView avatarView = ViewHolder.findViewById(conView, R.id.avatar);
    PlayButton playBtn = ViewHolder.findViewById(conView, R.id.playBtn);
    TextView locationView = ViewHolder.findViewById(conView, R.id.locationView);

    View statusSendFailed = ViewHolder.findViewById(conView, R.id.status_send_failed);
    View statusSendSucceed = ViewHolder.findViewById(conView, R.id.status_send_succeed);
    View statusSendStart = ViewHolder.findViewById(conView, R.id.status_send_start);

    // timestamp
    if (position == 0 || TimeUtils.haveTimeGap(datas.get(position - 1).getTimestamp(),
        msg.getTimestamp())) {
      sendTimeView.setVisibility(View.VISIBLE);
      sendTimeView.setText(TimeUtils.millisecs2DateString(msg.getTimestamp()));
    } else {
      sendTimeView.setVisibility(View.GONE);
    }

    String fromPeerId = msg.getFromPeerId();
    User user = App.lookupUser(fromPeerId);
    if (user == null) {
      throw new RuntimeException("cannot find user");
    }
    UserService.displayAvatar(user.getAvatarUrl(), avatarView);

    Msg.Type type = msg.getType();
    if (type == Msg.Type.Text) {
      contentView.setText(EmotionUtils.replace(ctx, msg.getContent()));
    } else if (type == Msg.Type.Image) {
      String localPath = PathUtils.getChatFileDir() + msg.getObjectId();
      String url = msg.getContent();
      displayImageByUri(imageView, localPath, url);
      setImageOnClickListener(localPath, url, imageView);
    } else if (type == Msg.Type.Audio) {
      initPlayBtn(msg, playBtn);
    } else if (type == Msg.Type.Location) {
      setLocationView(msg, locationView);
    }
    if (isComMsg == false) {
      hideStatusViews(statusSendStart, statusSendFailed, statusSendSucceed);
      setSendFailedBtnListener(statusSendFailed, msg);
      switch (msg.getStatus()) {
        case SendFailed:
          statusSendFailed.setVisibility(View.VISIBLE);
          break;
        case SendSucceed:
          statusSendSucceed.setVisibility(View.VISIBLE);
          break;
        case SendStart:
          statusSendStart.setVisibility(View.VISIBLE);
          break;
      }
      if (ChatActivity.roomType == RoomType.Group) {
        statusSendSucceed.setVisibility(View.GONE);
      }
    }
    return conView;
  }

  private void setSendFailedBtnListener(View statusSendFailed, final Msg msg) {
    statusSendFailed.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        chatActivity.resendMsg(msg);
      }
    });
  }

  private void hideStatusViews(View statusSendStart, View statusSendFailed, View statusSendSucceed) {
    statusSendFailed.setVisibility(View.GONE);
    statusSendStart.setVisibility(View.GONE);
    statusSendSucceed.setVisibility(View.GONE);
  }

  public void setLocationView(Msg msg, TextView locationView) {
    try {
      String content = msg.getContent();
      if (content != null && !content.equals("")) {
        String[] parts = content.split("&");
        String address = parts[0];
        final String latitude = parts[1];
        final String longtitude = parts[2];
        locationView.setText(address);
        locationView.setOnClickListener(new View.OnClickListener() {

          @Override
          public void onClick(View arg0) {
            Intent intent = new Intent(ctx, LocationActivity.class);
            intent.putExtra("type", "scan");
            intent.putExtra("latitude", Double.parseDouble(latitude));
            intent.putExtra("longtitude", Double.parseDouble(longtitude));
            ctx.startActivity(intent);
          }
        });
      }
    } catch (Exception e) {
    }
  }

  private void initPlayBtn(Msg msg, PlayButton playBtn) {
    playBtn.setLeftSide(msg.isComeMessage());
    AudioHelper audioHelper = AudioHelper.getInstance();
    playBtn.setAudioHelper(audioHelper);
    playBtn.setPath(msg.getAudioPath());
  }

  private void setImageOnClickListener(final String path, final String url, ImageView imageView) {
    imageView.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Intent intent = new Intent(ctx, ImageBrowerActivity.class);
        intent.putExtra("path", path);
        intent.putExtra("url", url);
        ctx.startActivity(intent);
      }
    });
  }

  public static void displayImageByUri(ImageView imageView,
                                       String localPath, String url) {
    File file = new File(localPath);
    ImageLoader imageLoader = UserService.imageLoader;
    if (file.exists()) {
      imageLoader.displayImage("file://" + localPath, imageView, PhotoUtil.normalImageOptions);
    } else {
      imageLoader.displayImage(url, imageView, PhotoUtil.normalImageOptions);
    }
  }

  public View createViewByType(int itemViewType, Msg.Type type, boolean isComeMsg) {
    int[] baseLayoutIds = new int[]{R.layout.chat_item_base_left, R.layout.chat_item_base_right};
    View baseView;
    if (isComeMsg) {
      baseView = inflater.inflate(R.layout.chat_item_base_left, null);
    } else {
      baseView = inflater.inflate(R.layout.chat_item_base_right, null);
    }
    LinearLayout contentView = (LinearLayout) baseView.findViewById(R.id.contentLayout);
    int contentId;
    switch (type) {
      case Text:
        contentId = R.layout.chat_item_text;
        break;
      case Audio:
        contentId = R.layout.chat_item_audio;
        break;
      case Image:
        contentId = R.layout.chat_item_image;
        break;
      case Location:
        contentId = R.layout.chat_item_location;
        break;
      default:
        throw new IllegalStateException();
    }
    contentView.removeAllViews();
    View content = inflater.inflate(contentId, null, false);
    if (type == Msg.Type.Audio) {
      PlayButton btn = (PlayButton) content;
      btn.setLeftSide(isComeMsg);
    } else if (type == Msg.Type.Text) {
      TextView textView = (TextView) content;
      if (isComeMsg) {
        textView.setTextColor(Color.BLACK);
      } else {
        textView.setTextColor(Color.WHITE);
      }
    }
    contentView.addView(content);
    return baseView;
  }
}
