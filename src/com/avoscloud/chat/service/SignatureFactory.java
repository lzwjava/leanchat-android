package com.avoscloud.chat.service;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.Signature;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * 请配合云代码使用
 */
public class SignatureFactory implements com.avos.avoscloud.SignatureFactory {
  @Override
  public Signature createSignature(String peerId, List<String> watchIds) {
    Signature s = new Signature();
    try {
      HashMap<String, Object> map = (HashMap<String, Object>) CloudService.sign(peerId, watchIds);

      List<String> watchIdsCopy = new ArrayList<String>();
      watchIdsCopy.addAll(watchIds);
      s.setSignedPeerIds(watchIdsCopy);
      setFields(s, map);
    } catch (AVException e) {
      e.printStackTrace();
    }
    return s;
  }

  public void setFields(Signature s, HashMap<String, Object> map) {
    int timestamp = (Integer) map.get("timestamp");
    String nonce = (String) map.get("nonce");
    String sig = (String) map.get("signature");
    //sig=sig+"haha"; test wrong sinature
    s.setTimestamp(timestamp);
    s.setNonce(nonce);
    s.setSignature(sig);
  }

  @Override
  public Signature createGroupSignature(String groupId, String peerId, List<String> targetPeerIds,
                                        String action) {
    Signature s = new Signature();
    List<String> watchIdsCopy = new ArrayList<String>();
    watchIdsCopy.addAll(targetPeerIds);
    try {
      HashMap<String, Object> map = (HashMap<String, Object>) CloudService.groupSign(peerId, groupId, targetPeerIds, action);
      setFields(s, map);
      s.setSignedPeerIds(watchIdsCopy);
    } catch (AVException e) {
      e.printStackTrace();
    }
    return s;
  }
}
