package com.avoscloud.chat.util;


import com.avoscloud.chat.entity.SortUser;

import java.util.Comparator;

public class PinyinComparator implements Comparator<SortUser> {
  public int compare(SortUser o1, SortUser o2) {
    if (o1.getSortLetters().equals("@")
        || o2.getSortLetters().equals("#")) {
      return -1;
    } else if (o1.getSortLetters().equals("#")
        || o2.getSortLetters().equals("@")) {
      return 1;
    } else {
      return o1.getSortLetters().compareTo(o2.getSortLetters());
    }
  }

}
