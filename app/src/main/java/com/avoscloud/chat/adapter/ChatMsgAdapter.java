package com.avoscloud.chat.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.im.v2.AVIMReservedMessageType;
import com.avos.avoscloud.im.v2.AVIMTypedMessage;
import com.avos.avoscloud.im.v2.messages.AVIMImageMessage;
import com.avos.avoscloud.im.v2.messages.AVIMLocationMessage;
import com.avos.avoscloud.im.v2.messages.AVIMTextMessage;
import com.avoscloud.chat.R;
import com.avoscloud.chat.avobject.User;
import com.avoscloud.chat.entity.ConvType;
import com.avoscloud.chat.service.AudioHelper;
import com.avoscloud.chat.service.CacheService;
import com.avoscloud.chat.service.UserService;
import com.avoscloud.chat.service.chat.MsgUtils;
import com.avoscloud.chat.ui.view.PlayButton;
import com.avoscloud.chat.ui.view.ViewHolder;
import com.avoscloud.chat.util.EmotionUtils;
import com.avoscloud.chat.util.PhotoUtils;
import com.avoscloud.chat.util.TimeUtils;

public class ChatMsgAdapter extends BaseListAdapter<AVIMTypedMessage> {
  private ConvType convType;
  private int msgViewTypes = 8;
  private ClickListener clickListener;

  public ChatMsgAdapter(Context context, ConvType convType) {
    super(context);
    this.convType = convType;
  }

  public void setClickListener(ClickListener clickListener) {
    this.clickListener = clickListener;
  }

  @Override
  public int getItemViewType(int position) {
    AVIMTypedMessage msg = datas.get(position);
    boolean comeMsg = isComeMsg(msg);

    MsgViewType viewType;
    AVIMReservedMessageType msgType = AVIMReservedMessageType.getAVIMReservedMessageType(msg.getMessageType());
    switch (msgType) {
      case TextMessageType:
        viewType = comeMsg ? MsgViewType.ComeText : MsgViewType.ToText;
        break;
      case ImageMessageType:
        viewType = comeMsg ? MsgViewType.ComeImage : MsgViewType.ToImage;
        break;
      case AudioMessageType:
        viewType = comeMsg ? MsgViewType.ComeAudio : MsgViewType.ToAudio;
        break;
      case LocationMessageType:
        viewType = comeMsg ? MsgViewType.ComeLocation : MsgViewType.ToLocation;
        break;
      default:
        throw new IllegalStateException();
    }
    return viewType.getValue();
  }

  @Override
  public int getViewTypeCount() {
    return msgViewTypes;
  }

  boolean isComeMsg(AVIMTypedMessage msg) {
    return !MsgUtils.fromMe(msg);
  }

  public View getView(int position, View conView, ViewGroup parent) {
    AVIMTypedMessage msg = datas.get(position);
    boolean isComMsg = isComeMsg(msg);
    if (conView == null) {
      conView = createViewByType(AVIMReservedMessageType.getAVIMReservedMessageType(msg.getMessageType()), isComMsg);
    }
    TextView sendTimeView = ViewHolder.findViewById(conView, R.id.sendTimeView);
    TextView contentView = ViewHolder.findViewById(conView, R.id.textContent);
    View contentLayout = ViewHolder.findViewById(conView, R.id.contentLayout);
    ImageView imageView = ViewHolder.findViewById(conView, R.id.imageView);
    ImageView avatarView = ViewHolder.findViewById(conView, R.id.avatar);
    PlayButton playBtn = ViewHolder.findViewById(conView, R.id.playBtn);
    TextView locationView = ViewHolder.findViewById(conView, R.id.locationView);
    TextView usernameView = ViewHolder.findViewById(conView, R.id.username);

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

    AVUser user = CacheService.lookupUser(msg.getFrom());
    if (user == null) {
      throw new NullPointerException("user is null");
    }
    if (isComMsg) {
      if (convType == null) {
        throw new NullPointerException("conv type is null");
      }
      if (convType == ConvType.Single) {
        usernameView.setVisibility(View.GONE);
      } else {
        usernameView.setVisibility(View.VISIBLE);
        usernameView.setText(user.getUsername());
      }
    }
    UserService.displayAvatar(User.getAvatarUrl(user), avatarView);

    AVIMReservedMessageType type = AVIMReservedMessageType.getAVIMReservedMessageType(msg.getMessageType());
    switch (type) {
      case TextMessageType:
        AVIMTextMessage textMsg = (AVIMTextMessage) msg;
        contentView.setText(EmotionUtils.replace(ctx, textMsg.getText()));
        contentLayout.requestLayout();
        break;
      case ImageMessageType:
        AVIMImageMessage imageMsg = (AVIMImageMessage) msg;
        PhotoUtils.displayImageCacheElseNetwork(imageView, MsgUtils.getFilePath(imageMsg),
            imageMsg.getFileUrl());
        setImageOnClickListener(imageView, imageMsg);
        break;
      case AudioMessageType:
        initPlayBtn(msg, playBtn);
        break;
      case LocationMessageType:
        setLocationView(msg, locationView);
        break;
      default:
        break;
    }
    if (isComMsg == false) {
      hideStatusViews(statusSendStart, statusSendFailed, statusSendSucceed);
      setSendFailedBtnListener(statusSendFailed, msg);
      switch (msg.getMessageStatus()) {
        case AVIMMessageStatusFailed:
          statusSendFailed.setVisibility(View.VISIBLE);
          break;
        case AVIMMessageStatusSent:
          if (convType == ConvType.Single) {
            statusSendSucceed.setVisibility(View.VISIBLE);
          }
          break;
        case AVIMMessageStatusNone:
        case AVIMMessageStatusSending:
          statusSendStart.setVisibility(View.VISIBLE);
          break;
        case AVIMMessageStatusReceipt:
          break;
      }
    }
    return conView;
  }

  private void setSendFailedBtnListener(View statusSendFailed, final AVIMTypedMessage msg) {
    statusSendFailed.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        if (clickListener != null) {
          clickListener.onFailButtonClick(msg);
        }
      }
    });
  }

  private void hideStatusViews(View statusSendStart, View statusSendFailed, View statusSendSucceed) {
    statusSendFailed.setVisibility(View.GONE);
    statusSendStart.setVisibility(View.GONE);
    statusSendSucceed.setVisibility(View.GONE);
  }

  public void setLocationView(AVIMTypedMessage msg, TextView locationView) {
    final AVIMLocationMessage locMsg = (AVIMLocationMessage) msg;
    locationView.setText(locMsg.getText());
    locationView.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View arg0) {
        if (clickListener != null) {
          clickListener.onLocationViewClick(locMsg);
        }
      }
    });
  }

  private void initPlayBtn(AVIMTypedMessage msg, PlayButton playBtn) {
    playBtn.setLeftSide(isComeMsg(msg));
    AudioHelper audioHelper = AudioHelper.getInstance();
    playBtn.setAudioHelper(audioHelper);
    playBtn.setPath(MsgUtils.getFilePath(msg));
  }

  private void setImageOnClickListener(ImageView imageView, final AVIMImageMessage imageMsg) {
    imageView.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        if (clickListener != null) {
          clickListener.onImageViewClick(imageMsg);
        }
      }
    });
  }

  public View createViewByType(AVIMReservedMessageType type, boolean comeMsg) {
    View baseView;
    if (comeMsg) {
      baseView = inflater.inflate(R.layout.chat_item_base_left, null);
    } else {
      baseView = inflater.inflate(R.layout.chat_item_base_right, null);
    }
    LinearLayout contentView = (LinearLayout) baseView.findViewById(R.id.contentLayout);
    int contentId;
    switch (type) {
      case TextMessageType:
        contentId = R.layout.chat_item_text;
        break;
      case AudioMessageType:
        contentId = R.layout.chat_item_audio;
        break;
      case ImageMessageType:
        contentId = R.layout.chat_item_image;
        break;
      case LocationMessageType:
        contentId = R.layout.chat_item_location;
        break;
      default:
        throw new IllegalStateException();
    }
    contentView.removeAllViews();
    View content = inflater.inflate(contentId, null, false);
    if (type == AVIMReservedMessageType.AudioMessageType) {
      PlayButton btn = (PlayButton) content;
      btn.setLeftSide(comeMsg);
    } else if (type == AVIMReservedMessageType.TextMessageType) {
      TextView textView = (TextView) content;
      if (comeMsg) {
        textView.setTextColor(Color.BLACK);
      } else {
        textView.setTextColor(Color.WHITE);
      }
    }
    contentView.addView(content);
    return baseView;
  }

  private enum MsgViewType {
    ComeText(0), ToText(1), ComeImage(2), ToImage(3), ComeAudio(4), ToAudio(5), ComeLocation(6), ToLocation(7);
    int value;

    MsgViewType(int value) {
      this.value = value;
    }

    public int getValue() {
      return value;
    }
  }

  public interface ClickListener {
    void onFailButtonClick(AVIMTypedMessage msg);

    void onLocationViewClick(AVIMLocationMessage locMsg);

    void onImageViewClick(AVIMImageMessage imageMsg);
  }
}
