package com.lzw.talk.entity;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.lzw.talk.base.C;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by lzw on 14-8-7.
 */
public class Msg {
  int created;
  String from;
  String to;
  String fromName;
  String txt;
  public Msg(){
  }

  public Msg(String json) {
    JSONObject jobj = (JSONObject) JSON.parse(json);
    from=jobj.getString(C.FROM);
    to=jobj.getString(C.TO);
    txt=jobj.getString(C.TXT);
    created=jobj.getInteger(C.CREATED);
  }

  public int getCreated() {
    return created;
  }

  public void setCreated(int created) {
    this.created = created;
  }

  public String getFrom() {
    return from;
  }

  public void setFrom(String from) {
    this.from = from;
  }

  public String getTo() {
    return to;
  }

  public void setTo(String to) {
    this.to = to;
  }

  public String getTxt() {
    return txt;
  }

  public void setTxt(String txt) {
    this.txt = txt;
  }

  public String getFromName() {
    return fromName;
  }

  public void setFromName(String fromName) {
    this.fromName = fromName;
  }

  public String toJson() {
    Map<String, Object> map = new HashMap<String, Object>();
    map.put(C.FROM, from);
    map.put(C.TO, to);
    map.put(C.TXT, txt);
    map.put(C.FROM_NAME,fromName);
    map.put(C.CREATED, created);
    JSONObject json = new JSONObject(map);
    return json.toJSONString();
  }
}
