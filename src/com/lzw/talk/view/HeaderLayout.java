package com.lzw.talk.view;

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

  public void setTitle(int titleId) {
    titleView.setText(titleId);
  }

  public void showLeftBackButton() {
    showLeftBackButton(R.string.emptyStr);
  }

  public void showLeftBackButton(int backTextId) {
    backBtn.setVisibility(View.VISIBLE);
    backBtn.setText(backTextId);
    backBtn.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        ((Activity) getContext()).finish();
      }
    });
  }
}
