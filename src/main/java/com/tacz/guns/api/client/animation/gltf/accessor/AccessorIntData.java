package com.tacz.guns.api.client.animation.gltf.accessor;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.Locale;

public final class AccessorIntData extends AbstractAccessorData implements AccessorData {
   private final boolean unsigned;

   public AccessorIntData(int componentType, ByteBuffer bufferViewByteBuffer, int byteOffset, int numElements, int numComponentsPerElement, Integer byteStride) {
      super(int.class, bufferViewByteBuffer, byteOffset, numElements, numComponentsPerElement, 4, byteStride);
      AccessorDatas.validateIntType(componentType);
      this.unsigned = AccessorDatas.isUnsignedType(componentType);
      AccessorDatas.validateCapacity(byteOffset, this.getNumElements(), this.getByteStridePerElement(), bufferViewByteBuffer.capacity());
   }

   public boolean isUnsigned() {
      return this.unsigned;
   }

   public int get(int elementIndex, int componentIndex) {
      int byteIndex = this.getByteIndex(elementIndex, componentIndex);
      return this.getBufferViewByteBuffer().getInt(byteIndex);
   }

   public int get(int globalComponentIndex) {
      int elementIndex = globalComponentIndex / this.getNumComponentsPerElement();
      int componentIndex = globalComponentIndex % this.getNumComponentsPerElement();
      return this.get(elementIndex, componentIndex);
   }

   public void set(int elementIndex, int componentIndex, int value) {
      int byteIndex = this.getByteIndex(elementIndex, componentIndex);
      this.getBufferViewByteBuffer().putInt(byteIndex, value);
   }

   public void set(int globalComponentIndex, int value) {
      int elementIndex = globalComponentIndex / this.getNumComponentsPerElement();
      int componentIndex = globalComponentIndex % this.getNumComponentsPerElement();
      this.set(elementIndex, componentIndex, value);
   }

   public long getLong(int elementIndex, int componentIndex) {
      int value = this.get(elementIndex, componentIndex);
      return this.unsigned ? Integer.toUnsignedLong(value) : value;
   }

   public long getLong(int globalComponentIndex) {
      int value = this.get(globalComponentIndex);
      return this.unsigned ? Integer.toUnsignedLong(value) : value;
   }

   public int[] computeMin() {
      int[] result = new int[this.getNumComponentsPerElement()];
      Arrays.fill(result, Integer.MAX_VALUE);

      for (int e = 0; e < this.getNumElements(); e++) {
         for (int c = 0; c < this.getNumComponentsPerElement(); c++) {
            result[c] = Math.min(result[c], this.get(e, c));
         }
      }

      return result;
   }

   public int[] computeMax() {
      int[] result = new int[this.getNumComponentsPerElement()];
      Arrays.fill(result, Integer.MIN_VALUE);

      for (int e = 0; e < this.getNumElements(); e++) {
         for (int c = 0; c < this.getNumComponentsPerElement(); c++) {
            result[c] = Math.max(result[c], this.get(e, c));
         }
      }

      return result;
   }

   public long[] computeMinLong() {
      long[] result = new long[this.getNumComponentsPerElement()];
      Arrays.fill(result, Long.MAX_VALUE);

      for (int e = 0; e < this.getNumElements(); e++) {
         for (int c = 0; c < this.getNumComponentsPerElement(); c++) {
            result[c] = Math.min(result[c], this.getLong(e, c));
         }
      }

      return result;
   }

   public long[] computeMaxLong() {
      long[] result = new long[this.getNumComponentsPerElement()];
      Arrays.fill(result, Long.MIN_VALUE);

      for (int e = 0; e < this.getNumElements(); e++) {
         for (int c = 0; c < this.getNumComponentsPerElement(); c++) {
            result[c] = Math.max(result[c], this.getLong(e, c));
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
         int component = this.get(i);
         result.putInt(component);
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

            long component = this.getLong(e, c);
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
