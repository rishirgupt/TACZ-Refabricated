package com.tacz.guns.client.resource.pojo.animation.gltf;

public class Buffer {
   private String uri;
   private Integer byteLength;

   public String getUri() {
      return this.uri;
   }

   public void setUri(String uri) {
      if (uri == null) {
         this.uri = uri;
      } else {
         this.uri = uri;
      }
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
}
