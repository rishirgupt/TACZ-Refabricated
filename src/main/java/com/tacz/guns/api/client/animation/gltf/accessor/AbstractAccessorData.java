package com.tacz.guns.api.client.animation.gltf.accessor;

import java.nio.ByteBuffer;
import java.util.Objects;

abstract class AbstractAccessorData implements AccessorData {
   private final Class<?> componentType;
   private final ByteBuffer bufferViewByteBuffer;
   private final int byteOffset;
   private final int numElements;
   private final int numComponentsPerElement;
   private final int numBytesPerComponent;
   private final int byteStridePerElement;

   AbstractAccessorData(
      Class<?> componentType,
      ByteBuffer bufferViewByteBuffer,
      int byteOffset,
      int numElements,
      int numComponentsPerElement,
      int numBytesPerComponent,
      Integer byteStride
   ) {
      Objects.requireNonNull(bufferViewByteBuffer, "The bufferViewByteBuffer is null");
      this.componentType = componentType;
      this.bufferViewByteBuffer = bufferViewByteBuffer;
      this.byteOffset = byteOffset;
      this.numElements = numElements;
      this.numComponentsPerElement = numComponentsPerElement;
      this.numBytesPerComponent = numBytesPerComponent;
      if (byteStride != null && byteStride != 0) {
         this.byteStridePerElement = byteStride;
      } else {
         this.byteStridePerElement = numComponentsPerElement * numBytesPerComponent;
      }
   }

   @Override
   public final Class<?> getComponentType() {
      return this.componentType;
   }

   @Override
   public final int getNumElements() {
      return this.numElements;
   }

   @Override
   public final int getNumComponentsPerElement() {
      return this.numComponentsPerElement;
   }

   @Override
   public final int getTotalNumComponents() {
      return this.numElements * this.numComponentsPerElement;
   }

   protected final int getByteIndex(int elementIndex, int componentIndex) {
      return this.byteOffset + elementIndex * this.byteStridePerElement + componentIndex * this.numBytesPerComponent;
   }

   protected final ByteBuffer getBufferViewByteBuffer() {
      return this.bufferViewByteBuffer;
   }

   protected final int getByteStridePerElement() {
      return this.byteStridePerElement;
   }

   protected final int getNumBytesPerComponent() {
      return this.numBytesPerComponent;
   }
}
