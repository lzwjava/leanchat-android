package com.avoscloud.chat.util;

import com.avos.avoscloud.AVMessage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by lzw on 14-9-15.
 */
public class AVOSUtils {
  public static String convid(String myId, String otherId) {
    List<String> ids;
    ids = new ArrayList<String>();
    ids.add(myId);
    ids.add(otherId);
    return convid(ids);
  }

  public static String convid(List<String> peerIds) {
    Collections.sort(peerIds);
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < peerIds.size(); i++) {
      if (i != 0) {
        sb.append(":");
      }
      sb.append(peerIds.get(i));
    }
    return Utils.md5(sb.toString());
  }

  public static void logAVMessage(AVMessage avMsg) {
    Logger.d("avMsg message="+avMsg.getMessage()+" timestamp="+avMsg.getTimestamp()+" toPeerIds="+avMsg.getToPeerIds
        ()+" fromPeerId="+avMsg.getFromPeerId()+" receiptTs="+avMsg.getReceiptTimestamp()+" groupId="+avMsg.getGroupId
        ()+" isRequestReceipt="+avMsg.isRequestReceipt());
  }
}
