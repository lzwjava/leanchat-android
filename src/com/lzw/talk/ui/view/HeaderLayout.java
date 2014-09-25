package com.lzw.talk.ui.view;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.lzw.talk.R;

/**
 * Created by lzw on 14-9-17.
 */
public class HeaderLayout extends LinearLayout {
  LayoutInflater mInflater;
  RelativeLayout header;
  TextView titleView;
  LinearLayout leftContainer, rightContainer;
  Button backBtn;

  public HeaderLayout(Context context) {
    super(context);
    init();
  }

  public HeaderLayout(Context context, AttributeSet attrs) {
    super(context, attrs);
    init();
  }

  private void init() {
    mInflater = LayoutInflater.from(getContext());
    header = (RelativeLayout) mInflater.inflate(R.layout.common_header, null, false);
    titleView = (TextView) header.findViewById(R.id.titleView);
    leftContainer = (LinearLayout) header.findViewById(R.id.leftContainer);
    rightContainer = (LinearLayout) header.findViewById(R.id.rightContainer);
    backBtn = (Button) header.findViewById(R.id.backBtn);
    addView(header);
  }

  public void showTitle(int titleId) {
    titleView.setText(titleId);
  }

  public void showTitle(String s) {
    titleView.setText(s);
  }

  public void showLeftBackButton(OnClickListener listener) {
    showLeftBackButton(R.string.emptyStr, listener);
  }

  public void showLeftBackButton(int backTextId, OnClickListener listener) {
    backBtn.setVisibility(View.VISIBLE);
    backBtn.setText(backTextId);
    if (listener == null) {
      listener = new OnClickListener() {
        @Override
        public void onClick(View v) {
          ((Activity) getContext()).finish();
        }
      };
    }
    backBtn.setOnClickListener(listener);
  }

  public void showRightImageButton(int rightResId, OnClickListener listener) {
    showRightButton(rightResId, listener, true);
  }

  public void showRightTextButton(int rightResId, OnClickListener listener) {
    showRightButton(rightResId, listener, false);
  }

  public void showRightButton(int rightResId, OnClickListener listener, boolean showImage) {
    View imageViewLayout = mInflater.inflate(R.layout.header_right_image_btn, null, false);
    Button rightButton = (Button) imageViewLayout.findViewById(R.id.imageView);
    if (showImage) {
      rightButton.setBackgroundResource(rightResId);
    } else {
      rightButton.setBackgroundColor(R.drawable.btn_login_selector);
      rightButton.setText(rightResId);
    }
    rightButton.setOnClickListener(listener);
    rightContainer.addView(imageViewLayout);
  }
}
