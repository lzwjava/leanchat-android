package com.avoscloud.chat.service;

import com.avos.avoscloud.AVCloud;
import com.avos.avoscloud.AVException;
import com.avoscloud.chat.avobject.User;
import com.avoscloud.chat.util.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Created by lzw on 14-9-29.
 */
public class CloudService {
   public static void removeFriendForBoth(User toUser, User fromUser) throws AVException {
    callCloudRelationFn(toUser, fromUser, "removeFriend");
  }

  public static void callCloudRelationFn(User toUser, User fromUser, String functionName) throws AVException {
    Map<String, Object> map = usersParamsMap(fromUser, toUser);
    Object res = AVCloud.callFunction(functionName, map);
  }

  public static Map<String, Object> usersParamsMap(User fromUser, User toUser) {
    Map<String, Object> map = new HashMap<String, Object>();
    map.put("fromUserId", fromUser.getObjectId());
    map.put("toUserId", toUser.getObjectId());
    return map;
  }

  public static void tryCreateAddRequest(User toUser) throws AVException {
    User user=User.curUser();
    Map<String,Object> map=usersParamsMap(user,toUser);
    AVCloud.callFunction("tryCreateAddRequest", map);
  }

  public static void agreeAddRequest(String objectId) throws AVException {
    Map<String, Object> map = new HashMap<String, Object>();
    map.put("objectId", objectId);
    AVCloud.callFunction("agreeAddRequest", map);
  }

  public static void saveChatGroup(String groupId, String ownerId, String name) throws AVException {
    Map<String,Object> map=new HashMap<String, Object>();
    map.put("groupId",groupId);
    map.put("ownerId",ownerId);
    map.put("name",name);
    AVCloud.callFunction("saveChatGroup",map);
  }
}
