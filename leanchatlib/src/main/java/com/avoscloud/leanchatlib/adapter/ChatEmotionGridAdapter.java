package com.avoscloud.leanchatlib.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.avoscloud.leanchatlib.R;
import com.avoscloud.leanchatlib.controller.EmotionHelper;
import com.avoscloud.leanchatlib.view.ViewHolder;

/**
 * Created by lzw on 14-9-25.
 */
public class ChatEmotionGridAdapter extends BaseListAdapter<String> {

  public ChatEmotionGridAdapter(Context ctx) {
    super(ctx);
  }

  @Override
  public View getView(int position, View conView, ViewGroup parent) {
    if (conView == null) {
      LayoutInflater inflater = LayoutInflater.from(ctx);
      conView = inflater.inflate(R.layout.chat_emotion_item, null);
    }
    ImageView emotionImageView = ViewHolder.findViewById(conView, R.id.emotionImageView);
    String emotion = (String) getItem(position);
    emotion = emotion.substring(1, emotion.length() - 1);
    Bitmap bitmap = EmotionHelper.getEmojiDrawable(ctx, emotion);
    emotionImageView.setImageBitmap(bitmap);
    return conView;
  }
}
