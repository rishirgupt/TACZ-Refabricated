package com.tacz.guns.api.client.animation.gltf;

import java.nio.ByteBuffer;

public class BufferModel {
   private String uri;
   private ByteBuffer bufferData;

   public String getUri() {
      return this.uri;
   }

   public void setUri(String uri) {
      this.uri = uri;
   }

   public int getByteLength() {
      return this.bufferData.capacity();
   }

   public ByteBuffer getBufferData() {
      return Buffers.createSlice(this.bufferData);
   }

   public void setBufferData(ByteBuffer bufferData) {
      this.bufferData = bufferData;
   }
}
