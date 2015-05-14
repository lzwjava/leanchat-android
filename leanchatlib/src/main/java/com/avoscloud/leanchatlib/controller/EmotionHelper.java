package com.avoscloud.leanchatlib.controller;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ImageSpan;
import com.avoscloud.leanchatlib.utils.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by lzw on 14-9-25.
 */
public class EmotionHelper {
  private static final int ONE_PAGE_SIZE = 21;
  public static List<List<String>> emojiGroups;
  private static Pattern pattern;
  private static String[] emojiCodes = new String[]{
      ":smile:",
      ":laughing:",
      ":blush:",
      ":smiley:",
      ":relaxed:",
      ":smirk:",
      ":heart_eyes:",
      ":kissing_heart:",
      ":kissing_closed_eyes:",
      ":flushed:",
      ":relieved:",
      ":satisfied:",
      ":grin:",
      ":wink:",
      ":stuck_out_tongue_winking_eye:",
      ":stuck_out_tongue_closed_eyes:",
      ":grinning:",
      ":kissing:",
      ":kissing_smiling_eyes:",
      ":stuck_out_tongue:",
      ":sleeping:",
      ":worried:",
      ":frowning:",
      ":anguished:",
      ":open_mouth:",
      ":grimacing:",
      ":confused:",
      ":hushed:",
      ":expressionless:",
      ":unamused:",
      ":sweat_smile:",
      ":sweat:",
      ":disappointed_relieved:",
      ":weary:",
      ":pensive:",
      ":disappointed:",
      ":confounded:",
      ":fearful:",
      ":cold_sweat:",
      ":persevere:",
      ":cry:",
      ":sob:",
      ":joy:",
      ":astonished:",
      ":scream:",
      ":tired_face:",
      ":angry:",
      ":rage:",
      ":triumph:",
      ":sleepy:",
      ":yum:",
      ":mask:",
      ":sunglasses:",
      ":dizzy_face:",
      ":neutral_face:",
      ":no_mouth:",
      ":innocent:",
      ":thumbsup:",
      ":thumbsdown:",
      ":clap:",
      ":point_right:",
      ":point_left:"};

  static {
    int pages = emojiCodes.length / ONE_PAGE_SIZE + (emojiCodes.length % ONE_PAGE_SIZE == 0 ? 0 : 1);
    emojiGroups = new ArrayList<>();
    for (int page = 0; page < pages; page++) {
      List<String> onePageEmojis = new ArrayList<>();
      int start = page * ONE_PAGE_SIZE;
      int end = Math.min(page * ONE_PAGE_SIZE + ONE_PAGE_SIZE, emojiCodes.length);
      for (int i = start; i < end; i++) {
        onePageEmojis.add(emojiCodes[i]);
      }
      emojiGroups.add(onePageEmojis);
    }
    pattern = Pattern.compile("\\:[a-z0-9-_]*\\:");
  }

  public static boolean contain(String[] strings, String string) {
    for (String s : strings) {
      if (s.equals(string)) {
        return true;
      }
    }
    return false;
  }

  public static CharSequence replace(Context context, String text) {
    if (TextUtils.isEmpty(text)) {
      return text;
    }
    SpannableString spannableString = new SpannableString(text);
    Matcher matcher = pattern.matcher(text);
    while (matcher.find()) {
      String factText = matcher.group();
      String key = factText.substring(1, factText.length() - 1);
      if (contain(emojiCodes, factText)) {
        Bitmap bitmap = getEmojiDrawable(context, key);
        ImageSpan image = new ImageSpan(context, bitmap);
        int start = matcher.start();
        int end = matcher.end();
        spannableString.setSpan(image, start, end,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
      }
    }
    return spannableString;
  }

  public static void isEmojiDrawableAvailable() {
    for (String emojiCode : emojiCodes) {
      String code = emojiCode.substring(1, emojiCode.length() - 1);
      Bitmap bitmap = getDrawableByName(ChatManager.getContext(), code);
      if (bitmap == null) {
        Utils.log("not available test " + code);
      }
    }
  }

  public static Bitmap getEmojiDrawable(Context context, String name) {
    return getDrawableByName(context, "emoji_" + name);
  }

  public static Bitmap getDrawableByName(Context ctx, String name) {
    BitmapFactory.Options options = new BitmapFactory.Options();
    Bitmap bitmap = BitmapFactory.decodeResource(ctx.getResources(),
        ctx.getResources().getIdentifier(name, "drawable",
            ctx.getPackageName()), options);
    return bitmap;
  }
}

