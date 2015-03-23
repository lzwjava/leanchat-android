/**
 * Copyright 2013 Omar Miatello - omar.miatello@justonetouch.it
 * Based on http://stackoverflow.com/a/18000094/1228545
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.avoscloud.chat.util;

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