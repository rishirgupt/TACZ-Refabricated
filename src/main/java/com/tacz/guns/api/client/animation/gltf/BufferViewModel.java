package com.tacz.guns.api.client.animation.gltf;

import java.nio.ByteBuffer;
import java.util.function.Consumer;

public class BufferViewModel {
   private final Integer target;
   private BufferModel bufferModel;
   private int byteOffset = 0;
   private int byteLength = 0;
   private Integer byteStride;
   private Consumer<? super ByteBuffer> sparseSubstitutionCallback;
   private boolean sparseSubstitutionApplied;

   public BufferViewModel(Integer target) {
      this.target = target;
   }

   public void setSparseSubstitutionCallback(Consumer<? super ByteBuffer> sparseSubstitutionCallback) {
      this.sparseSubstitutionCallback = sparseSubstitutionCallback;
   }

   public ByteBuffer getBufferViewData() {
      ByteBuffer bufferData = this.bufferModel.getBufferData();
      ByteBuffer bufferViewData = Buffers.createSlice(bufferData, this.getByteOffset(), this.getByteLength());
      if (this.sparseSubstitutionCallback != null && !this.sparseSubstitutionApplied) {
         this.sparseSubstitutionCallback.accept(bufferViewData);
         this.sparseSubstitutionApplied = true;
      }

      return bufferViewData;
   }

   public BufferModel getBufferModel() {
      return this.bufferModel;
   }

   public void setBufferModel(BufferModel bufferModel) {
      this.bufferModel = bufferModel;
   }

   public int getByteOffset() {
      return this.byteOffset;
   }

   public void setByteOffset(int byteOffset) {
      this.byteOffset = byteOffset;
   }

   public int getByteLength() {
      return this.byteLength;
   }

   public void setByteLength(int byteLength) {
      this.byteLength = byteLength;
   }

   public Integer getByteStride() {
      return this.byteStride;
   }

   public void setByteStride(Integer byteStride) {
      this.byteStride = byteStride;
   }

   public Integer getTarget() {
      return this.target;
   }
}
