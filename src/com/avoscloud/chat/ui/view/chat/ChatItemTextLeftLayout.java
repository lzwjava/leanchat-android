package com.avoscloud.chat.ui.view.chat;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import com.avoscloud.chat.R;

/**
 * Created by lzw on 14/12/19.
 */
public class ChatItemTextLeftLayout extends LinearLayout {
  public ChatItemTextLeftLayout(Context context, AttributeSet attrs) {
    super(context, attrs);
    View baseLayout = inflate(context, R.layout.chat_item_base_left, null);
    LinearLayout contentView = (LinearLayout) baseLayout.findViewById(R.id.contentLayout);
    View textView = inflate(context, R.layout.chat_item_text, null);
    contentView.addView(textView);
    addView(baseLayout);
  }
}

