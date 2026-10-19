package com.tacz.guns.api.client.animation.gltf.accessor;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.Locale;

public final class AccessorFloatData extends AbstractAccessorData implements AccessorData {
   public AccessorFloatData(
      int componentType, ByteBuffer bufferViewByteBuffer, int byteOffset, int numElements, int numComponentsPerElement, Integer byteStride
   ) {
      super(float.class, bufferViewByteBuffer, byteOffset, numElements, numComponentsPerElement, 4, byteStride);
      AccessorDatas.validateFloatType(componentType);
      AccessorDatas.validateCapacity(byteOffset, this.getNumElements(), this.getByteStridePerElement(), bufferViewByteBuffer.capacity());
   }

   public float get(int elementIndex, int componentIndex) {
      int byteIndex = this.getByteIndex(elementIndex, componentIndex);
      return this.getBufferViewByteBuffer().getFloat(byteIndex);
   }

   public float get(int globalComponentIndex) {
      int elementIndex = globalComponentIndex / this.getNumComponentsPerElement();
      int componentIndex = globalComponentIndex % this.getNumComponentsPerElement();
      return this.get(elementIndex, componentIndex);
   }

   public void set(int elementIndex, int componentIndex, float value) {
      int byteIndex = this.getByteIndex(elementIndex, componentIndex);
      this.getBufferViewByteBuffer().putFloat(byteIndex, value);
   }

   public void set(int globalComponentIndex, float value) {
      int elementIndex = globalComponentIndex / this.getNumComponentsPerElement();
      int componentIndex = globalComponentIndex % this.getNumComponentsPerElement();
      this.set(elementIndex, componentIndex, value);
   }

   public float[] computeMin() {
      float[] result = new float[this.getNumComponentsPerElement()];
      Arrays.fill(result, Float.MAX_VALUE);

      for (int e = 0; e < this.getNumElements(); e++) {
         for (int c = 0; c < this.getNumComponentsPerElement(); c++) {
            result[c] = Math.min(result[c], this.get(e, c));
         }
      }

      return result;
   }

   public float[] computeMax() {
      float[] result = new float[this.getNumComponentsPerElement()];
      Arrays.fill(result, -Float.MAX_VALUE);

      for (int e = 0; e < this.getNumElements(); e++) {
         for (int c = 0; c < this.getNumComponentsPerElement(); c++) {
            result[c] = Math.max(result[c], this.get(e, c));
         }
      }

      return result;
   }

   @Override
   public ByteBuffer createByteBuffer() {
      int totalNumComponents = this.getTotalNumComponents();
      int totalBytes = totalNumComponents * this.getNumBytesPerComponent();
      ByteBuffer result = ByteBuffer.allocateDirect(totalBytes).order(ByteOrder.nativeOrder());

      for (int i = 0; i < totalNumComponents; i++) {
         float component = this.get(i);
         result.putFloat(component);
      }

      result.position(0);
      return result;
   }

   public String createString(Locale locale, String format, int elementsPerRow) {
      StringBuilder sb = new StringBuilder();
      int nc = this.getNumComponentsPerElement();
      sb.append("[");

      for (int e = 0; e < this.getNumElements(); e++) {
         if (e > 0) {
            sb.append(", ");
            if (elementsPerRow > 0 && e % elementsPerRow == 0) {
               sb.append("\n ");
            }
         }

         if (nc > 1) {
            sb.append("(");
         }

         for (int c = 0; c < nc; c++) {
            if (c > 0) {
               sb.append(", ");
            }

            float component = this.get(e, c);
            sb.append(String.format(locale, format, component));
         }

         if (nc > 1) {
            sb.append(")");
         }
      }

      sb.append("]");
      return sb.toString();
   }
}
