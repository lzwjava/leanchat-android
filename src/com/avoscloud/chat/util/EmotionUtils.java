package com.avoscloud.chat.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ImageSpan;
import android.text.style.RelativeSizeSpan;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by lzw on 14-9-25.
 */
public class EmotionUtils {
  public static List<String> emotionTexts;
  public static List<String> emotionTexts1;
  public static List<String> emotionTexts2;

  private static List<String> emotions;
  public static int[] emotionCodes = new int[]{0x1F601, 0x1F602, 0x1F603, 0x1F604, 0x1F605, 0x1F606, 0x1F609, 0x1F60A, 0x1F60B, 0x1F60C,
      0x1F60D, 0x1F60F, 0x1F612, 0x1F613, 0x1F614, 0x1F616, 0x1F618, 0x1F61A, 0x1F61C, 0x1F61D, 0x1F61E, 0x1F620, 0x1F621, 0x1F622, 0x1F623, 0x1F624,
      0x1F625, 0x1F628, 0x1F629, 0x1F62A, 0x1F62B, 0x1F62D, 0x1F630, 0x1F631, 0x1F632, 0x1F633, 0x1F635, 0x1F637};
  public static List<String> emotions1, emotions2;
  public static String[] emojiCodes = new String[]{"\\u1f60a", "\\u1f60c",
      "\\u1f60d", "\\u1f60f", "\\u1f61a", "\\u1f61b", "\\u1f61c", "\\u1f61e", "\\u1f62a", "\\u1f601", "\\u1f602", "\\u1f603",
      "\\u1f604", "\\u1f609", "\\u1f612", "\\u1f613", "\\u1f614", "\\u1f616", "\\u1f618", "\\u1f620", "\\u1f621", "\\u1f622",
      "\\u1f621", "\\u1f622", "\\u1f623", "\\u1f625", "\\u1f628", "\\u1f630", "\\u1f631", "\\u1f632", "\\u1f633", "\\u1f637",
      "\\u1f44d", "\\u1f44e", "\\u1f44f"};

  static String getEmojiByUnicode(int unicode) {
    return new String(Character.toChars(unicode));
  }

  private static Pattern pattern;

  static {
    emotions = new ArrayList<String>();
    int i;
    for (i = 0; i < emotionCodes.length; i++) {
      emotions.add(getEmojiByUnicode(emotionCodes[i]));
    }
    emotions1 = emotions.subList(0, 21);
    emotions2 = emotions.subList(21, emotions.size());

    emotionTexts = new ArrayList<String>();
    for (String emojiCode : emojiCodes) {
      emotionTexts.add(emojiCode);
    }
    emotionTexts1 = emotionTexts.subList(0, 21);
    emotionTexts2 = emotionTexts.subList(21, emotionTexts.size());
    pattern = buildPattern();
  }

  private static Pattern buildPattern() {
    return Pattern.compile("\\\\u1f[a-z0-9]{3}");
  }

  public static boolean haveEmotion(String text) {
    Matcher matcher = pattern.matcher(text);
    if (matcher.find()) {
      return true;
    } else {
      return false;
    }
  }

  public static CharSequence scaleEmotions(String text) {
    SpannableString spannableString = new SpannableString(text);
    for (String emotion : emotions) {
      Pattern pattern = Pattern.compile(emotion);
      Matcher matcher = pattern.matcher(text);
      while (matcher.find()) {
        int start = matcher.start();
        int end = matcher.end();
        spannableString.setSpan(new RelativeSizeSpan(1.2f), start, end, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
      }
    }
    return spannableString;
  }

  public static CharSequence replace(Context ctx, String text) {
    SpannableString spannableString = new SpannableString(text);
    Matcher matcher = pattern.matcher(text);
    while (matcher.find()) {
      String factText = matcher.group();
      String key = factText.substring(1);
      if (emotionTexts.contains(factText)) {
        Bitmap bitmap = getDrawableByName(ctx, key);
        ImageSpan image = new ImageSpan(ctx, bitmap);
        int start = matcher.start();
        int end = matcher.end();
        spannableString.setSpan(image, start, end,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
      }
    }
    return spannableString;
  }

  public static Bitmap getDrawableByName(Context ctx, String name) {
    BitmapFactory.Options options = new BitmapFactory.Options();
    Bitmap bitmap = BitmapFactory.decodeResource(ctx.getResources(),
        ctx.getResources().getIdentifier(name, "drawable",
            ctx.getPackageName()), options);
    return bitmap;
  }
}

