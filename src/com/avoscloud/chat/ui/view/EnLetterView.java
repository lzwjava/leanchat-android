package com.avoscloud.chat.ui.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import com.avoscloud.chat.util.PixelUtil;
import com.avoscloud.chat.R;

public class EnLetterView extends View {
  private OnTouchingLetterChangedListener onTouchingLetterChangedListener;
  public static String[] letters = {"A", "B", "C", "D", "E", "F", "G", "H", "I",
      "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V",
      "W", "X", "Y", "Z", "#" };
  private int choose = -1;// 閫変腑
  private Paint paint = new Paint();

  private TextView textDialog;

  public void setTextView(TextView mTextDialog) {
    this.textDialog = mTextDialog;
  }

  public EnLetterView(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
  }

  public EnLetterView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public EnLetterView(Context context) {
    super(context);
  }

  protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);
    int height = getHeight();
    int width = getWidth();
    int singleHeight = height / letters.length;

    for (int i = 0; i < letters.length; i++) {
      paint.setColor(getResources().getColor(R.color.color_bottom_text_normal));
      paint.setTypeface(Typeface.DEFAULT_BOLD);
      paint.setAntiAlias(true);
      paint.setTextSize(PixelUtil.sp2px(12));
      if (i == choose) {
        paint.setColor(Color.parseColor("#3399ff"));
        paint.setFakeBoldText(true);
      }
      float xPos = width / 2 - paint.measureText(letters[i]) / 2;
      float yPos = singleHeight * i + singleHeight;
      canvas.drawText(letters[i], xPos, yPos, paint);
      paint.reset();
    }

  }

  @Override
  public boolean dispatchTouchEvent(MotionEvent event) {
    final int action = event.getAction();
    final float y = event.getY();
    final int oldChoose = choose;
    final OnTouchingLetterChangedListener listener = onTouchingLetterChangedListener;
    final int c = (int) (y / getHeight() * letters.length);

    switch (action) {
      case MotionEvent.ACTION_UP:
        setBackgroundDrawable(new ColorDrawable(0x00000000));
        choose = -1;
        invalidate();
        if (textDialog != null) {
          textDialog.setVisibility(View.INVISIBLE);
        }
        break;

      default:
        setBackgroundResource(R.drawable.v2_sortlistview_sidebar_background);
        if (oldChoose != c) {
          if (c >= 0 && c < letters.length) {
            if (listener != null) {
              listener.onTouchingLetterChanged(letters[c]);
            }
            if (textDialog != null) {
              textDialog.setText(letters[c]);
              textDialog.setVisibility(View.VISIBLE);
            }

            choose = c;
            invalidate();
          }
        }

        break;
    }
    return true;
  }

  public void setOnTouchingLetterChangedListener(
      OnTouchingLetterChangedListener onTouchingLetterChangedListener) {
    this.onTouchingLetterChangedListener = onTouchingLetterChangedListener;
  }

  public interface OnTouchingLetterChangedListener {
    public void onTouchingLetterChanged(String s);
  }

}
