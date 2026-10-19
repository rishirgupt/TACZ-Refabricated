package com.tacz.guns.api.client.animation.gltf.accessor;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.Locale;

public final class AccessorShortData extends AbstractAccessorData implements AccessorData {
   private final boolean unsigned;

   public AccessorShortData(
      int componentType, ByteBuffer bufferViewByteBuffer, int byteOffset, int numElements, int numComponentsPerElement, Integer byteStride
   ) {
      super(short.class, bufferViewByteBuffer, byteOffset, numElements, numComponentsPerElement, 2, byteStride);
      AccessorDatas.validateShortType(componentType);
      this.unsigned = AccessorDatas.isUnsignedType(componentType);
      AccessorDatas.validateCapacity(byteOffset, this.getNumElements(), this.getByteStridePerElement(), bufferViewByteBuffer.capacity());
   }

   public boolean isUnsigned() {
      return this.unsigned;
   }

   public short get(int elementIndex, int componentIndex) {
      int byteIndex = this.getByteIndex(elementIndex, componentIndex);
      return this.getBufferViewByteBuffer().getShort(byteIndex);
   }

   public short get(int globalComponentIndex) {
      int elementIndex = globalComponentIndex / this.getNumComponentsPerElement();
      int componentIndex = globalComponentIndex % this.getNumComponentsPerElement();
      return this.get(elementIndex, componentIndex);
   }

   public void set(int elementIndex, int componentIndex, short value) {
      int byteIndex = this.getByteIndex(elementIndex, componentIndex);
      this.getBufferViewByteBuffer().putShort(byteIndex, value);
   }

   public void set(int globalComponentIndex, short value) {
      int elementIndex = globalComponentIndex / this.getNumComponentsPerElement();
      int componentIndex = globalComponentIndex % this.getNumComponentsPerElement();
      this.set(elementIndex, componentIndex, value);
   }

   public int getInt(int elementIndex, int componentIndex) {
      short value = this.get(elementIndex, componentIndex);
      return this.unsigned ? Short.toUnsignedInt(value) : value;
   }

   public int getInt(int globalComponentIndex) {
      short value = this.get(globalComponentIndex);
      return this.unsigned ? Short.toUnsignedInt(value) : value;
   }

   public short[] computeMin() {
      short[] result = new short[this.getNumComponentsPerElement()];
      Arrays.fill(result, (short)32767);

      for (int e = 0; e < this.getNumElements(); e++) {
         for (int c = 0; c < this.getNumComponentsPerElement(); c++) {
            result[c] = (short)Math.min(result[c], this.get(e, c));
         }
      }

      return result;
   }

   public short[] computeMax() {
      short[] result = new short[this.getNumComponentsPerElement()];
      Arrays.fill(result, (short)-32768);

      for (int e = 0; e < this.getNumElements(); e++) {
         for (int c = 0; c < this.getNumComponentsPerElement(); c++) {
            result[c] = (short)Math.max(result[c], this.get(e, c));
         }
      }

      return result;
   }

   public int[] computeMinInt() {
      int[] result = new int[this.getNumComponentsPerElement()];
      Arrays.fill(result, Integer.MAX_VALUE);

      for (int e = 0; e < this.getNumElements(); e++) {
         for (int c = 0; c < this.getNumComponentsPerElement(); c++) {
            result[c] = Math.min(result[c], this.getInt(e, c));
         }
      }

      return result;
   }

   public int[] computeMaxInt() {
      int[] result = new int[this.getNumComponentsPerElement()];
      Arrays.fill(result, Integer.MIN_VALUE);

      for (int e = 0; e < this.getNumElements(); e++) {
         for (int c = 0; c < this.getNumComponentsPerElement(); c++) {
            result[c] = Math.max(result[c], this.getInt(e, c));
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
         short component = this.get(i);
         result.putShort(component);
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

            int component = this.getInt(e, c);
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
