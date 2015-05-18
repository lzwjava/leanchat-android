package com.avoscloud.chat.service;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.LruCache;
import com.avoscloud.chat.util.PhotoUtils;

import java.util.Random;

/**
 * Created by lzw on 15/5/18.
 */
public class ColoredBitmapProvider {
  private LruCache<String, Bitmap> memoryCache;

  private ColoredBitmapProvider() {
    int cacheSize = ((int) Runtime.getRuntime().maxMemory()) / 10;
    memoryCache = new LruCache<String, Bitmap>(cacheSize) {
      @Override
      protected int sizeOf(String key, Bitmap value) {
        return value.getByteCount();
      }
    };
  }

  private static ColoredBitmapProvider instance;

  public static synchronized ColoredBitmapProvider getInstance() {
    if (instance == null) {
      instance = new ColoredBitmapProvider();
    }
    return instance;
  }

  public Bitmap createColoredBitmapByHashString(String hashString) {
    if (hashString.length() % 3 != 0) {
      throw new IllegalArgumentException("should be fully divided by 3");
    }
    if (memoryCache.get(hashString) != null) {
      return memoryCache.get(hashString);
    }
    int partLength = hashString.length() / 3;
    String part1 = hashString.substring(0, partLength);
    String part2 = hashString.substring(partLength, partLength * 2);
    String part3 = hashString.substring(partLength * 2, partLength * 3);
    Bitmap colorBitmap = createBrightColorBitmap(positiveHashCode(part1) % 360,
        positiveHashCode(part2) % 128, positiveHashCode(part3) % 128);
    memoryCache.put(hashString, colorBitmap);
    return colorBitmap;
  }

  private static int positiveHashCode(String s) {
    int code = s.hashCode();
    if (code < 0) {
      return -code;
    } else {
      return code;
    }
  }

  public static Bitmap createRandomColoredBitmap() {
    Random random = new Random(System.currentTimeMillis());
    int hueValue = random.nextInt(360) % 360;
    int saturationValue = random.nextInt(128) % 128;
    int brightnessValue = random.nextInt(128) % 128;
    return createBrightColorBitmap(hueValue, saturationValue, brightnessValue);
  }

  private static Bitmap createBrightColorBitmap(int hueValue, int saturationValue, int brightnessValue) {
    float hue = hueValue;
    float saturation = saturationValue / 256.0f + 0.5f;
    float brightness = brightnessValue / 256.0f + 0.5f;
    return createColoredBitmap(hue, saturation, brightness);
  }

  private static Bitmap createColoredBitmap(float hue, float saturation, float brightness) {
    int color = Color.HSVToColor(new float[]{hue, saturation, brightness});
    Bitmap bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.RGB_565);
    bitmap.eraseColor(color);
    Bitmap resultBitmap = PhotoUtils.toRoundCorner(bitmap, 10);
    bitmap.recycle();
    return resultBitmap;
  }
}
