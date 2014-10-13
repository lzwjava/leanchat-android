package com.lzw.talk.service;

import com.avos.avoscloud.*;
import com.lzw.talk.avobject.ChatGroup;
import com.lzw.talk.avobject.User;
import com.lzw.talk.base.App;
import com.lzw.talk.base.C;

import java.util.Arrays;
import java.util.List;

/**
 * Created by lzw on 14-10-8.
 */
public class GroupService {
  public static final String GROUP_ID = "groupId";

  public static List<ChatGroup> findGroups() throws AVException {
    User user = User.curUser();
    AVQuery<ChatGroup> q = AVObject.getQuery(ChatGroup.class);
    q.whereEqualTo(ChatGroup.M, user.getObjectId());
    q.include(ChatGroup.OWNER);
    return q.find();
  }

  public static boolean isGroupOwner(ChatGroup chatGroup, User user) {
    return chatGroup.getOwner().equals(user);
  }

  public static void inviteMembers(ChatGroup chatGroup, List<User> users) {
    Group group = getGroup(chatGroup);
    List<String> userIds = UserService.transformIds(users);
    group.inviteMember(userIds);
  }

  public static Group getGroup(ChatGroup chatGroup) {
    Session session = ChatService.getSession();
    return session.getGroup(chatGroup.getObjectId());
  }

  public static void kickMember(ChatGroup chatGroup, User member) {
    Group group = getGroup(chatGroup);
    group.kickMember(Arrays.asList(member.getObjectId()));
  }

  public static ChatGroup setNewChatGroupData(String groupId, String newGroupName) throws AVException {
    ChatGroup chatGroup = ChatGroup.createWithoutData(ChatGroup.class, groupId);
    User user = User.curUser();
    chatGroup.setOwner(user);

    AVACL acl = new AVACL();  // just owner can add member
    acl.setPublicWriteAccess(false);
    acl.setWriteAccess(user, true);
    acl.setPublicReadAccess(true);
    chatGroup.setACL(acl);
    chatGroup.setName(newGroupName);
    chatGroup.setFetchWhenSave(true);
    chatGroup.save();
    return chatGroup;
  }

  public static void cacheChatGroups(List<String> ids) throws AVException {
    findChatGroups(ids);
  }

  private static List<ChatGroup> findChatGroups(List<String> ids) throws AVException {
    AVQuery<ChatGroup> q = AVObject.getQuery(ChatGroup.class);
    q.whereContainedIn(C.OBJECT_ID, ids);
    q.include(ChatGroup.OWNER);
    List<ChatGroup> chatGroups = q.find();
    App.registerChatGroupsCache(chatGroups);
    return chatGroups;
  }
}
