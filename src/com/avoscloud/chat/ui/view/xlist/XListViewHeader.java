package com.avoscloud.chat.ui.view.xlist;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import com.avoscloud.chat.R;

public class XListViewHeader extends LinearLayout {
  private LinearLayout mContainer;
  private ImageView mArrowImageView;
  private ProgressBar mProgressBar;
  private int mState = STATE_NORMAL;

  private Animation mRotateUpAnim;
  private Animation mRotateDownAnim;

  private final int ROTATE_ANIM_DURATION = 180;

  public final static int STATE_NORMAL = 0;
  public final static int STATE_READY = 1;
  public final static int STATE_REFRESHING = 2;

  public XListViewHeader(Context context) {
    super(context);
    initView(context);
  }

  public XListViewHeader(Context context, AttributeSet attrs) {
    super(context, attrs);
    initView(context);
  }

  private void initView(Context context) {
    // 初始情况，设置下拉刷新view高度为0
    LayoutParams lp = new LayoutParams(
        android.view.ViewGroup.LayoutParams.MATCH_PARENT, 0);
    mContainer = (LinearLayout) LayoutInflater.from(context).inflate(
        R.layout.xlistview_header, null);
    addView(mContainer, lp);
    setGravity(Gravity.BOTTOM);

    mArrowImageView = (ImageView) findViewById(R.id.xlistview_header_arrow);
    mProgressBar = (ProgressBar) findViewById(R.id.xlistview_header_progressbar);

    mRotateUpAnim = new RotateAnimation(0.0f, -180.0f,
        Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
        0.5f);
    mRotateUpAnim.setDuration(ROTATE_ANIM_DURATION);
    mRotateUpAnim.setFillAfter(true);
    mRotateDownAnim = new RotateAnimation(-180.0f, 0.0f,
        Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
        0.5f);
    mRotateDownAnim.setDuration(ROTATE_ANIM_DURATION);
    mRotateDownAnim.setFillAfter(true);
  }

  public void setState(int state) {
    if (state == mState) return;

    if (state == STATE_REFRESHING) {  // 显示进度
      mArrowImageView.clearAnimation();
      mArrowImageView.setVisibility(View.INVISIBLE);
      mProgressBar.setVisibility(View.VISIBLE);
    } else {  // 显示箭头图片
      mArrowImageView.setVisibility(View.VISIBLE);
      mProgressBar.setVisibility(View.INVISIBLE);
    }

    switch (state) {
      case STATE_NORMAL:
        if (mState == STATE_READY) {
          mArrowImageView.startAnimation(mRotateDownAnim);
        }
        if (mState == STATE_REFRESHING) {
          mArrowImageView.clearAnimation();
        }
        break;
      case STATE_READY:
        if (mState != STATE_READY) {
          mArrowImageView.clearAnimation();
          mArrowImageView.startAnimation(mRotateUpAnim);
        }
        break;
      case STATE_REFRESHING:
        break;
      default:
    }

    mState = state;
  }

  public void setVisiableHeight(int height) {
    if (height < 0)
      height = 0;
    LayoutParams lp = (LayoutParams) mContainer
        .getLayoutParams();
    lp.height = height;
    mContainer.setLayoutParams(lp);
  }

  public int getVisiableHeight() {
    return mContainer.getHeight();
  }

}
