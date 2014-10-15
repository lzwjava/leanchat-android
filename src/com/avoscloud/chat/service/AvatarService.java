package com.avoscloud.chat.service;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVFile;
import com.avos.avoscloud.AVObject;

import java.io.FileNotFoundException;
import java.util.Random;

/**
 * Created by lzw on 14-10-15.
 */
public class AvatarService {
  private static String[] avatarFileIds = new String[]{
      "543e8008e4b073603c0a849c",
      "543e8016e4b073603c0a84ea",
      "543e801be4b073603c0a8508",
      "543e8020e4b073603c0a8528",
      "543e8028e4b073603c0a8543",
      "543e802de4b073603c0a855b",
      "543e8032e4b073603c0a8574",
      "543e8037e4b073603c0a8587",
      "543e803ce4b073603c0a85a5",
      "543e8042e4b073603c0a85c9",
      "543e8048e4b073603c0a85eb"};

  public static AVFile getRandomAvatarFile() throws AVException, FileNotFoundException {
    int pos = new Random().nextInt(avatarFileIds.length);
    return AVFile.withObjectId(avatarFileIds[pos]);
  }
}
