package com.tacz.guns.api.client.animation.gltf;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class Buffers {
   private Buffers() {
   }

   public static ByteBuffer createSlice(ByteBuffer byteBuffer) {
      return byteBuffer == null ? null : byteBuffer.slice().order(byteBuffer.order());
   }

   public static ByteBuffer createSlice(ByteBuffer byteBuffer, int position, int length) {
      if (byteBuffer == null) {
         return null;
      }

      int oldPosition = byteBuffer.position();
      int oldLimit = byteBuffer.limit();

      try {
         int newLimit = position + length;
         if (newLimit > byteBuffer.capacity()) {
            throw new IllegalArgumentException("The new limit is " + newLimit + ", but the capacity is " + byteBuffer.capacity());
         }

         byteBuffer.limit(newLimit);
         byteBuffer.position(position);
         ByteBuffer slice = byteBuffer.slice();
         slice.order(byteBuffer.order());
         return slice;
      } finally {
         byteBuffer.limit(oldLimit);
         byteBuffer.position(oldPosition);
      }
   }

   public static ByteBuffer create(byte[] data) {
      return create(data, 0, data.length);
   }

   public static ByteBuffer create(byte[] data, int offset, int length) {
      ByteBuffer byteBuffer = ByteBuffer.allocateDirect(length);
      byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
      byteBuffer.put(data, offset, length);
      byteBuffer.position(0);
      return byteBuffer;
   }

   public static ByteBuffer create(int size) {
      ByteBuffer byteBuffer = ByteBuffer.allocateDirect(size);
      byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
      return byteBuffer;
   }
}
