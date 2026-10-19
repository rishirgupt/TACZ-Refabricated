package com.tacz.guns.api.client.animation.gltf.accessor;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.Locale;

public final class AccessorByteData extends AbstractAccessorData implements AccessorData {
   private final boolean unsigned;

   public AccessorByteData(int componentType, ByteBuffer bufferViewByteBuffer, int byteOffset, int numElements, int numComponentsPerElement, Integer byteStride) {
      super(byte.class, bufferViewByteBuffer, byteOffset, numElements, numComponentsPerElement, 1, byteStride);
      AccessorDatas.validateByteType(componentType);
      this.unsigned = AccessorDatas.isUnsignedType(componentType);
      AccessorDatas.validateCapacity(byteOffset, this.getNumElements(), this.getByteStridePerElement(), bufferViewByteBuffer.capacity());
   }

   public boolean isUnsigned() {
      return this.unsigned;
   }

   public byte get(int elementIndex, int componentIndex) {
      int byteIndex = this.getByteIndex(elementIndex, componentIndex);
      return this.getBufferViewByteBuffer().get(byteIndex);
   }

   public byte get(int globalComponentIndex) {
      int elementIndex = globalComponentIndex / this.getNumComponentsPerElement();
      int componentIndex = globalComponentIndex % this.getNumComponentsPerElement();
      return this.get(elementIndex, componentIndex);
   }

   public void set(int elementIndex, int componentIndex, byte value) {
      int byteIndex = this.getByteIndex(elementIndex, componentIndex);
      this.getBufferViewByteBuffer().put(byteIndex, value);
   }

   public void set(int globalComponentIndex, byte value) {
      int elementIndex = globalComponentIndex / this.getNumComponentsPerElement();
      int componentIndex = globalComponentIndex % this.getNumComponentsPerElement();
      this.set(elementIndex, componentIndex, value);
   }

   public int getInt(int elementIndex, int componentIndex) {
      byte value = this.get(elementIndex, componentIndex);
      return this.unsigned ? Byte.toUnsignedInt(value) : value;
   }

   public int getInt(int globalComponentIndex) {
      byte value = this.get(globalComponentIndex);
      return this.unsigned ? Byte.toUnsignedInt(value) : value;
   }

   public byte[] computeMin() {
      byte[] result = new byte[this.getNumComponentsPerElement()];
      Arrays.fill(result, (byte)127);

      for (int e = 0; e < this.getNumElements(); e++) {
         for (int c = 0; c < this.getNumComponentsPerElement(); c++) {
            result[c] = (byte)Math.min(result[c], this.get(e, c));
         }
      }

      return result;
   }

   public byte[] computeMax() {
      byte[] result = new byte[this.getNumComponentsPerElement()];
      Arrays.fill(result, (byte)-128);

      for (int e = 0; e < this.getNumElements(); e++) {
         for (int c = 0; c < this.getNumComponentsPerElement(); c++) {
            result[c] = (byte)Math.max(result[c], this.get(e, c));
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
         byte component = this.get(i);
         result.put(component);
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
