package com.avoscloud.leanchatlib.utils;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * How to use<a></>
 * - Make a simple object (POJO)
 * - Create a parcelable in ONE CLICK! http://devk.it/proj/parcelabler/
 * - Convert to pojo <-> byte[]
 * <p/>
 * Example
 * MyParcelable happy = new MyParcelable();
 * byte[] toByte = ParcelableUtil.marshall(happy);
 * // Save to DB? Send via socket?
 * // ...
 * // Restore from DB?
 * byte[] fromByte = cursor.getBlob(c);
 * MyParcelable happy = ParcelableUtil.unmarshall(fromByte, MyParcelable.CREATOR);
 */
public class ParcelableUtil {
  public static byte[] marshall(Parcelable parceable) {
    Parcel parcel = Parcel.obtain();
    parceable.writeToParcel(parcel, 0);
    byte[] bytes = parcel.marshall();
    parcel.recycle(); // not sure if needed or a good idea
    return bytes;
  }

  public static <T extends Parcelable> T unmarshall(byte[] bytes, Parcelable.Creator<T> creator) {
    Parcel parcel = unmarshall(bytes);
    return creator.createFromParcel(parcel);
  }

  public static Parcel unmarshall(byte[] bytes) {
    Parcel parcel = Parcel.obtain();
    parcel.unmarshall(bytes, 0, bytes.length);
    parcel.setDataPosition(0); // this is extremely important!
    return parcel;
  }
}