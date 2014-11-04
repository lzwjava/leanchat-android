package com.avoscloud.chat.ui.view;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.EditText;
import com.avoscloud.chat.util.EmotionUtils;

public class EmotionEditText extends EditText {

  public EmotionEditText(Context context) {
    super(context);
  }

  public EmotionEditText(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
  }

  public EmotionEditText(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  @Override
  public void setText(CharSequence text, BufferType type) {
    if (!TextUtils.isEmpty(text)) {
      super.setText(EmotionUtils.replace(getContext(), text.toString()), type);
    } else {
      super.setText(text, type);
    }
  }
}
