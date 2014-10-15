package com.avoscloud.chat.ui.view;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import com.avoscloud.chat.R;
import com.avoscloud.chat.base.App;

import java.io.File;
import java.io.IOException;

public class RecordButton extends Button {
  public static final int BACK_RECORDING = R.drawable.chat_voice_bg_pressed;
  public static final int BACK_IDLE = R.drawable.chat_voice_bg;

  public RecordButton(Context context) {
    super(context);
    init();
  }

  public RecordButton(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
    init();
  }

  public RecordButton(Context context, AttributeSet attrs) {
    super(context, attrs);
    init();
  }

  public void setSavePath(String path) {
    outputPath = path;
  }

  public void setOnFinishedRecordListener(RecordEventListener listener) {
    finishedListener = listener;
  }

  private String outputPath = null;

  private RecordEventListener finishedListener;

  private static final int MIN_INTERVAL_TIME = 1000;// 2s
  private long startTime;

  private Dialog recordIndicator;

  private static int[] res = {R.drawable.mic_2, R.drawable.mic_3,
      R.drawable.mic_4, R.drawable.mic_5};
  private static ImageView view;
  private MediaRecorder recorder;
  private ObtainDecibelThread thread;
  private Handler volumeHandler;

  private void init() {
    volumeHandler = new ShowVolumeHandler();
    setBackgroundResource(BACK_IDLE);
  }

  @Override
  public boolean onTouchEvent(MotionEvent event) {

    if (outputPath == null)
      return false;
    int action = event.getAction();
    switch (action) {
      case MotionEvent.ACTION_DOWN:
        initDialogAndStartRecord();
        break;
      case MotionEvent.ACTION_UP:
        finishRecord();
        break;
      case MotionEvent.ACTION_CANCEL:// 褰撴墜鎸囩Щ鍔ㄥ埌view澶栭潰锛屼細cancel
        cancelRecord();
        break;
    }
    return true;
  }

  private void initDialogAndStartRecord() {

    startTime = System.currentTimeMillis();
    recordIndicator = new Dialog(getContext(),
        R.style.like_toast_dialog_style);
    view = new ImageView(getContext());
    view.setImageResource(R.drawable.mic_2);
    recordIndicator.setContentView(view, new LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.MATCH_PARENT));
    recordIndicator.setOnDismissListener(onDismiss);
    LayoutParams lp = recordIndicator.getWindow().getAttributes();
    lp.gravity = Gravity.CENTER;
    setBackgroundResource(BACK_RECORDING);
    startRecording();
    recordIndicator.show();
  }

  private void finishRecord() {
    stopRecording();
    recordIndicator.dismiss();
    setBackgroundResource(BACK_IDLE);

    long intervalTime = System.currentTimeMillis() - startTime;
    if (intervalTime < MIN_INTERVAL_TIME) {
      Toast.makeText(getContext(), getContext().getString(R.string.pleaseSayMore), Toast.LENGTH_SHORT).show();
      File file = new File(outputPath);
      file.delete();
      return;
    }

    int sec = Math.round(intervalTime * 1.0f / 1000);
    if (finishedListener != null) {
      finishedListener.onFinishedRecord(outputPath, sec);
    }
  }

  private void cancelRecord() {
    stopRecording();
    recordIndicator.dismiss();

    Toast.makeText(getContext(), App.ctx.getString(R.string.cancelRecord),
        Toast.LENGTH_SHORT).show();
    File file = new File(outputPath);
    file.delete();
  }

  private void startRecording() {
    if (recorder == null) {
      recorder = new MediaRecorder();
      recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
      recorder.setOutputFormat(MediaRecorder.OutputFormat.RAW_AMR);
      recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
      recorder.setOutputFile(outputPath);
      try {
        recorder.prepare();
      } catch (IOException e) {
        e.printStackTrace();
      }
    } else {
      recorder.reset();
      recorder.setOutputFile(outputPath);
    }
    recorder.start();
    thread = new ObtainDecibelThread();
    thread.start();
    finishedListener.onStartRecord();
  }

  private void stopRecording() {
    if (thread != null) {
      thread.exit();
      thread = null;
    }
    if (recorder != null) {
      recorder.stop();
      recorder.release();
      recorder = null;
    }
  }

  private class ObtainDecibelThread extends Thread {

    private volatile boolean running = true;

    public void exit() {
      running = false;
    }

    @Override
    public void run() {
      while (running) {
        try {
          Thread.sleep(200);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
        if (recorder == null || !running) {
          break;
        }
        int x = recorder.getMaxAmplitude();
        if (x != 0) {
          int f = (int) (10 * Math.log(x) / Math.log(10));
          if (f < 26)
            volumeHandler.sendEmptyMessage(0);
          else if (f < 32)
            volumeHandler.sendEmptyMessage(1);
          else if (f < 38)
            volumeHandler.sendEmptyMessage(2);
          else
            volumeHandler.sendEmptyMessage(3);
        }
      }
    }

  }

  private OnDismissListener onDismiss = new OnDismissListener() {

    @Override
    public void onDismiss(DialogInterface dialog) {
      stopRecording();
    }
  };

  static class ShowVolumeHandler extends Handler {
    @Override
    public void handleMessage(Message msg) {
      view.setImageResource(res[msg.what]);
    }
  }

  public interface RecordEventListener {
    public void onFinishedRecord(String audioPath, int secs);

    void onStartRecord();
  }
}
