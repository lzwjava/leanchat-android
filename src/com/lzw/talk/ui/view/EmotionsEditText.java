package com.lzw.talk.ui.view;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.EditText;
import com.lzw.talk.service.EmotionService;

public class EmotionsEditText extends EditText {

  public EmotionsEditText(Context context) {
    super(context);
  }

  public EmotionsEditText(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
  }

  public EmotionsEditText(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  @Override
  public void setText(CharSequence text, BufferType type) {
    if (!TextUtils.isEmpty(text)) {
      super.setText(EmotionService.replace(getContext(), text.toString()), type);
    } else {
      super.setText(text, type);
    }
  }
}
