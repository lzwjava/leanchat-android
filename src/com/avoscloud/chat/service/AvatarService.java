package com.avoscloud.chat.service;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVFile;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.avoscloud.chat.avobject.Avatar;

import java.io.FileNotFoundException;
import java.util.Random;

/**
 * Created by lzw on 14-10-15.
 */
public class AvatarService {

  public static AVFile getRandomAvatarFile() throws AVException, FileNotFoundException {
    int cnt = 11;
    int pos = new Random().nextInt(cnt);
    AVQuery<Avatar> q = Avatar.getQuery(Avatar.class);
    q.setLimit(1);
    q.orderByAscending("createdAt");
    q.skip(pos);
    q.setCachePolicy(AVQuery.CachePolicy.NETWORK_ELSE_CACHE);
    return q.getFirst().getFile();
  }
}
