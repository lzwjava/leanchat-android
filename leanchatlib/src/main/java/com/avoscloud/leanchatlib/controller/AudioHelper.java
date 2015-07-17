package com.avoscloud.leanchatlib.controller;

import android.media.MediaPlayer;
import com.avoscloud.leanchatlib.utils.LogUtils;

import java.io.IOException;

/**
 * Created by lzw on 14/12/19.
 */
public class AudioHelper {
  private static AudioHelper audioHelper;
  private MediaPlayer mediaPlayer;
  private Runnable finishCallback;
  private String audioPath;
  private boolean onceStart = false;

  private AudioHelper() {
    mediaPlayer = new MediaPlayer();
  }

  public static AudioHelper getInstance() {
    if (audioHelper == null) {
      audioHelper = new AudioHelper();
    }
    return audioHelper;
  }

  public String getAudioPath() {
    return audioPath;
  }

  public void stopPlayer() {
    if (mediaPlayer != null) {
      mediaPlayer.stop();
      mediaPlayer.release();
      mediaPlayer = null;
    }
  }

  public void pausePlayer() {
    if (mediaPlayer != null) {
      mediaPlayer.pause();
    }
  }

  public boolean isPlaying() {
    return mediaPlayer.isPlaying();
  }

  public void restartPlayer() {
    if (mediaPlayer != null && mediaPlayer.isPlaying() == false) {
      mediaPlayer.start();
    }
  }

  public synchronized void playAudio(String path, Runnable finishCallback) {
    if (onceStart) {
      mediaPlayer.reset();
    }
    tryRunFinishCallback();
    audioPath = path;
    AudioHelper.this.finishCallback = finishCallback;
    try {
      mediaPlayer.setDataSource(path);
      mediaPlayer.prepare();
      mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mp) {
          tryRunFinishCallback();
        }
      });
      mediaPlayer.start();
      onceStart = true;
    } catch (IOException e) {
      LogUtils.logException(e);
    }
  }

  public void tryRunFinishCallback() {
    if (finishCallback != null) {
      finishCallback.run();
      finishCallback = null;
    }
  }
}
