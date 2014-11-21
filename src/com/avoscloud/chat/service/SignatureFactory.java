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
    try {
      HashMap<String, Object> map = CloudService.sign(peerId, watchIds);
      return createSignature(map, watchIds);
    } catch (AVException e) {
      e.printStackTrace();
    }
    return null;
  }

  Signature createSignature(HashMap<String, Object> params, List<String> peerIds) {
    Signature s = new Signature();
    List<String> copyIds = new ArrayList<String>();
    copyIds.addAll(peerIds);
    s.setTimestamp((Integer) params.get("timestamp"));
    s.setNonce((String) params.get("nonce"));
    s.setSignature((String) params.get("signature"));
    s.setSignedPeerIds(copyIds);
    return s;
  }

  @Override
  public Signature createGroupSignature(String groupId, String peerId, List<String> targetPeerIds,
                                        String action) {
    try {
      HashMap<String, Object> map = CloudService.groupSign(peerId, groupId, targetPeerIds, action);
      return createSignature(map, targetPeerIds);
    } catch (AVException e) {
      e.printStackTrace();
    }
    return null;
  }
}
