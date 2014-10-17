package com.avoscloud.chat.ui.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.media.MediaPlayer;
import android.util.AttributeSet;
import android.view.View;
import com.avoscloud.chat.R;
import com.avoscloud.chat.util.Utils;

import java.io.IOException;

/**
 * Created by lzw on 14-9-22.
 */
public class PlayButton extends View implements View.OnClickListener {
  MediaPlayer mediaPlayer;
  String path;
  Context ctx;
  boolean prepared = false;
  int backResourceId;

  public PlayButton(Context context) {
    super(context);
    ctx = context;
    init();
  }

  public PlayButton(Context context, AttributeSet attrs) {
    super(context, attrs);
    ctx = context;
    TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.PlayBtn);
    boolean left = true;
    for (int i = 0; i < typedArray.getIndexCount(); i++) {
      int attr = typedArray.getIndex(i);
      switch (attr) {
        case R.styleable.PlayBtn_left:
          left = typedArray.getBoolean(attr, true);
          break;
      }
    }
    if (left) {
      backResourceId = R.drawable.voice_left;
    } else {
      backResourceId = R.drawable.voice_right;
    }
    init();
  }

  public void setPath(String path) {
    this.path = path;
    prepared = false;
  }

  public void playAudio() {
    if (prepared == false) {
      try {
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setDataSource(path);
        mediaPlayer.prepare();
        mediaPlayer.start();
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
          @Override
          public void onCompletion(MediaPlayer mp) {
            setBackgroundResource(backResourceId);
          }
        });
        setBackgroundDrawable(null);
        prepared = true;
      } catch (IOException e) {
        e.printStackTrace();
        Utils.toast(ctx, e.getMessage());
      }
    } else {
      mediaPlayer.start();
      setBackgroundDrawable(null);
    }
  }

  private void init() {
    setBackgroundResource(backResourceId);
    setOnClickListener(this);
  }

  @Override
  protected void onDetachedFromWindow() {
    if (mediaPlayer != null) {
      mediaPlayer.stop();
      mediaPlayer.release();
      mediaPlayer = null;
    }
    super.onDetachedFromWindow();
  }

  @Override
  public void onClick(View v) {
    if (mediaPlayer != null && mediaPlayer.isPlaying()) {
      mediaPlayer.pause();
      setBackgroundResource(backResourceId);
    } else {
      playAudio();
    }
  }
}
