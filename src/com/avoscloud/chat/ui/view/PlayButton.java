package com.avoscloud.chat.ui.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.AnimationDrawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import com.avoscloud.chat.R;
import com.avoscloud.chat.service.AudioHelper;

/**
 * Created by lzw on 14-9-22.
 */
public class PlayButton extends ImageView implements View.OnClickListener {
  String path;
  Context ctx;
  boolean isLeft;
  AnimationDrawable anim;
  AudioHelper audioHelper;

  public void setAudioHelper(AudioHelper audioHelper) {
    this.audioHelper = audioHelper;
  }


  public PlayButton(Context context, AttributeSet attrs) {
    super(context, attrs);
    ctx = context;
    isLeft = getLeftAttributeValue(context, attrs);
    stopRecordAnimation();
    setOnClickListener(this);

  }

  public boolean getLeftAttributeValue(Context context, AttributeSet attrs) {
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
    return left;
  }

  public void setPath(String path) {
    this.path = path;
  }

  @Override
  public void onClick(View v) {
    if(audioHelper==null){
      throw new NullPointerException();
    }
    if(audioHelper.isPlaying()==true && audioHelper.getAudioPath().equals(path)){
      audioHelper.pausePlayer();
      stopRecordAnimation();
    }

    if (mediaPlayer != null && mediaPlayer.isPlaying()) {
      mediaPlayer.pause();
      setBackgroundResource(backResourceId);
    } else {
      playAudio(path);
    }
  }

  private void startRecordAnimation() {
    if (isLeft) {
      setImageResource(R.anim.anim_chat_voice_left);
    } else {
      setImageResource(R.anim.anim_chat_voice_right);
    }
    anim = (AnimationDrawable) getDrawable();
    anim.start();
  }

  private void stopRecordAnimation() {
    if (isLeft) {
      setImageResource(R.drawable.voice_right3);
    } else {
      setImageResource(R.drawable.voice_left3);
    }
    if (anim != null) {
      anim.stop();
    }
  }
}
