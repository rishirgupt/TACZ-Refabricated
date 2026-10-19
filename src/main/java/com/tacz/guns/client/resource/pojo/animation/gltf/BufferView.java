package com.tacz.guns.client.resource.pojo.animation.gltf;

public class BufferView {
   private Integer buffer;
   private Integer byteOffset;
   private Integer byteLength;
   private Integer byteStride;
   private Integer target;

   public Integer getBuffer() {
      return this.buffer;
   }

   public void setBuffer(Integer buffer) {
      if (buffer == null) {
         throw new NullPointerException("Invalid value for buffer: " + buffer + ", may not be null");
      }

      this.buffer = buffer;
   }

   public Integer getByteOffset() {
      return this.byteOffset;
   }

   public void setByteOffset(Integer byteOffset) {
      if (byteOffset == null) {
         this.byteOffset = byteOffset;
      } else {
         if (byteOffset < 0) {
            throw new IllegalArgumentException("byteOffset < 0");
         }

         this.byteOffset = byteOffset;
      }
   }

   public Integer defaultByteOffset() {
      return 0;
   }

   public Integer getByteLength() {
      return this.byteLength;
   }

   public void setByteLength(Integer byteLength) {
      if (byteLength == null) {
         throw new NullPointerException("Invalid value for byteLength: " + byteLength + ", may not be null");
      }

      if (byteLength < 1) {
         throw new IllegalArgumentException("byteLength < 1");
      }

      this.byteLength = byteLength;
   }

   public Integer getByteStride() {
      return this.byteStride;
   }

   public void setByteStride(Integer byteStride) {
      if (byteStride == null) {
         this.byteStride = byteStride;
      } else {
         if (byteStride > 252) {
            throw new IllegalArgumentException("byteStride > 252");
         }

         if (byteStride < 4) {
            throw new IllegalArgumentException("byteStride < 4");
         }

         this.byteStride = byteStride;
      }
   }

   public Integer getTarget() {
      return this.target;
   }

   public void setTarget(Integer target) {
      if (target == null) {
         this.target = target;
      } else {
         if (target != 34962 && target != 34963) {
            throw new IllegalArgumentException("Invalid value for target: " + target + ", valid: [34962, 34963]");
         }

         this.target = target;
      }
   }
}
