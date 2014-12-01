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
  public static String[] emojiCodes=new String[]{"\\ue058","\\ue057","\\ue056","\\ue059","\\ue105","\\ue106",
      "\\ue107","\\ue108", "\\ue401","\\ue402","\\ue403","\\ue404","\\ue405","\\ue406","\\ue407","\\ue408","\\ue409",
      "\\ue40a","\\ue40b","\\ue40d","\\ue40e","\\ue40f","\\ue410","\\ue411","\\ue412","\\ue413",
      "\\ue414","\\ue415","\\ue416","\\ue417","\\ue418","\\ue41f","\\ue00e","\\ue421"};

  static String getEmojiByUnicode(int unicode) {
    return new String(Character.toChars(unicode));
  }

  static {
    emotions = new ArrayList<String>();
    int i;
    for (i = 0; i < emotionCodes.length; i++) {
      emotions.add(getEmojiByUnicode(emotionCodes[i]));
    }
    emotions1 = emotions.subList(0, 21);
    emotions2 = emotions.subList(21, emotions.size());

    emotionTexts=new ArrayList<String>();
    for(String emojiCode : emojiCodes){
      emotionTexts.add(emojiCode);
    }
    emotionTexts1=emotionTexts.subList(0,21);
    emotionTexts2=emotionTexts.subList(21,emotionTexts.size());
  }

  private static Pattern buildPattern() {
    return Pattern.compile("\\\\ue[a-z0-9]{3}", Pattern.CASE_INSENSITIVE);
  }

  public static boolean haveEmotion(String text) {
    Matcher matcher = buildMatcher(text);
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
    try {
      SpannableString spannableString = new SpannableString(text);
      int start = 0;
      Matcher matcher = buildMatcher(text);
      while (matcher.find()) {
        String faceText = matcher.group();
        String key = faceText.substring(1);
        BitmapFactory.Options options = new BitmapFactory.Options();
        Bitmap bitmap = BitmapFactory.decodeResource(ctx.getResources(),
            ctx.getResources().getIdentifier(key, "drawable",
                ctx.getPackageName()), options);
        ImageSpan imageSpan = new ImageSpan(ctx, bitmap);
        int startIndex = text.indexOf(faceText, start);
        int endIndex = startIndex + faceText.length();
        if (startIndex >= 0) {
          spannableString.setSpan(imageSpan, startIndex, endIndex,
              Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        start = (endIndex - 1);
      }
      return spannableString;
    } catch (Exception e) {
      return text;
    }
  }

  public static Matcher buildMatcher(String text) {
    Pattern pattern = buildPattern();
    return pattern.matcher(text);
  }
}
