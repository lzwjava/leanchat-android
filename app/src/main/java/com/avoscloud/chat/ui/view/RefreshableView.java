package com.avoscloud.chat.ui.view;

import android.content.Context;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.RotateAnimation;
import android.widget.*;
import com.avoscloud.chat.R;

/**
 * Created by lzw on 14-4-23.
 */
public class RefreshableView extends LinearLayout implements View.OnTouchListener {
  public static final int PULL_TO_REFRESH = 0;
  public static final int RELEASE_TO_REFRESH = 1;
  public static final int ROLLBACK_VECLOCITY = -10;
  public static final int REFRESHING = 2;
  public static final int IDLE = 3;
  View attachedView;
  float downY;
  private boolean loadOnce = false;
  int touchSlop;
  View header;
  MarginLayoutParams headerMargins;
  int hideHeaderTop;
  TextView tipsView;
  ImageView arrow;
  ProgressBar progressBar;
  int status;
  RefreshListener refreshListener;
  int lastStatus;

  public RefreshableView(Context context, AttributeSet attrs) {
    super(context, attrs);
    touchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
    header = inflate(getContext(), R.layout.pull_to_refresh_header, null);
    tipsView = (TextView) header.findViewById(R.id.tips);
    arrow = (ImageView) header.findViewById(R.id.arrow);
    progressBar = (ProgressBar) header.findViewById(R.id.progressBar);
    addView(header);
  }

  @Override
  protected void onLayout(boolean changed, int l, int t, int r, int b) {
    super.onLayout(changed, l, t, r, b);
    if (changed && !loadOnce) {
      attachedView = getChildAt(1);
      attachedView.setOnTouchListener(this);
      hideHeaderTop = -header.getHeight();
      headerMargins = new MarginLayoutParams(header.getLayoutParams());
      setMargin(hideHeaderTop);
      status=IDLE;
      loadOnce = true;
    }
  }

  boolean isSlop=false;

  @Override
  public boolean onTouch(View v, MotionEvent event) {
    if(refreshListener.isAbleToPull()==false){
      return false;
    }
    int action = event.getAction();
    if (action == MotionEvent.ACTION_DOWN) {
      downY = event.getRawY();
    } else if (action == MotionEvent.ACTION_MOVE) {
      float curY = event.getRawY();
      int deltaY = (int) (curY - downY);
      if (deltaY < touchSlop){
        status=IDLE;
        isSlop=true;
        return false;
      }
      isSlop=false;
      int topMargin = deltaY/2;
      int top = topMargin + hideHeaderTop;
      if (top < hideHeaderTop) {
        top = hideHeaderTop;
      }
      if (top > 0) {
        status = RELEASE_TO_REFRESH;
      } else if (top < 0) {
        status = PULL_TO_REFRESH;
      }
      setMargin(top);
      if(status==PULL_TO_REFRESH || status==RELEASE_TO_REFRESH){
        updateHeaderView();
        attachedView.setFocusable(false);
        attachedView.setFocusableInTouchMode(false);
        attachedView.setPressed(false);
        lastStatus=status;
      }
    } else if (action == MotionEvent.ACTION_UP) {
      if (status == RELEASE_TO_REFRESH) {
        new RefreshingTask().execute();
      } else if (status == PULL_TO_REFRESH) {
        new HideHeaderTask().execute();
      }
    }
    if(isSlop){
      return false;
    }
    return true;
  }

  class HideHeaderTask extends AsyncTask<Void,Integer,Void>{

    @Override
    protected void onProgressUpdate(Integer... values) {
      super.onProgressUpdate(values);
      int top=values[0];
      setMargin(top);
    }

    @Override
    protected Void doInBackground(Void... params) {
      int top = headerMargins.topMargin;
      int v=ROLLBACK_VECLOCITY;
      while (true) {
        if (top > hideHeaderTop) {
          top += v;
        }
        if (top < hideHeaderTop) {
          top = hideHeaderTop;
        }
        publishProgress(top);
        if (top == hideHeaderTop) {
          break;
        }
        int time=10;
        sleep(time);
      }
      return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
      super.onPostExecute(aVoid);
      status=IDLE;
    }
  }

  public void refresh(){
    new RefreshingTask().execute();
  }

  class RefreshingTask extends AsyncTask<Void, Integer, Void> {

    @Override
    protected void onPreExecute() {
      super.onPreExecute();
      status = REFRESHING;
      updateHeaderView();
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
      super.onProgressUpdate(values);
      int top = values[0];
      setMargin(top);
    }

    @Override
    protected Void doInBackground(Void... params) {
      int v = ROLLBACK_VECLOCITY;
      int top = headerMargins.topMargin;
      while (true) {
        if (top > 0) {
          top += v;
        }
        if (top < 0) {
          top = 0;
        }
        publishProgress(top);
        if (top == 0) {
          break;
        }
        int time=10;
        sleep(time);
      }
      if (refreshListener != null) {
        refreshListener.onRefresh();
      }
      return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
      super.onPostExecute(aVoid);
      updateHeaderView();
    }
  }

  public void finishRefreshing(){
    new HideHeaderTask().execute();
    status = IDLE;
  }

  private void sleep(int time) {
    try {
      Thread.sleep(time);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  private void updateHeaderView() {
    if(lastStatus==status){
      return;
    }
    if (status == PULL_TO_REFRESH) {
      tipsView.setText(R.string.pull_to_refresh);
      progressBar.setVisibility(View.GONE);
      arrow.setVisibility(View.VISIBLE);
      rotateArrow();
    } else if (status == RELEASE_TO_REFRESH) {
      tipsView.setText(R.string.release_to_refresh);
      progressBar.setVisibility(View.GONE);
      arrow.setVisibility(View.VISIBLE);
      rotateArrow();
    } else if (status == REFRESHING) {
      tipsView.setText(R.string.refreshing);
      arrow.clearAnimation();
      arrow.setVisibility(GONE);
      progressBar.setVisibility(View.VISIBLE);
    }
  }

  private void rotateArrow() {
    float pivotX = arrow.getWidth() / 2;
    float pivotY = arrow.getHeight() / 2;
    float to=0f;
    float from=0f;
    if (status == RELEASE_TO_REFRESH) {
      from = 0f;
      to = 180f;
    } else if(status==PULL_TO_REFRESH){
      from = 180f;
      to = 360f;
    }
    RotateAnimation anim = new RotateAnimation(from, to, pivotX, pivotY);
    anim.setFillAfter(true);
    anim.setDuration(100);
    arrow.startAnimation(anim);
  }

  private void setMargin(int top) {
    headerMargins.topMargin = top;
    LayoutParams params = new LayoutParams(headerMargins);
    header.setLayoutParams(params);
  }

  public void scrollLittle(){
    setMargin(0);
    new HideHeaderTask().execute();
  }

  public interface RefreshListener {
    void onRefresh();

    boolean isAbleToPull();
  }

  public void setRefreshListener(RefreshListener refreshListener) {
    this.refreshListener = refreshListener;
  }

  public static abstract class ListRefreshListener implements RefreshableView.RefreshListener{
    ListView listView;

    public ListRefreshListener(ListView listView) {
      this.listView = listView;
    }

    @Override
    public boolean isAbleToPull() {
      View first=listView.getChildAt(0);
      return first!=null && listView.getFirstVisiblePosition()==0
          && first.getTop()==0;
    }
  }
}
