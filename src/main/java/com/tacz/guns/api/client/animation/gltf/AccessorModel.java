package com.tacz.guns.api.client.animation.gltf;

import com.tacz.guns.api.client.animation.gltf.accessor.AccessorData;
import com.tacz.guns.api.client.animation.gltf.accessor.AccessorDatas;
import com.tacz.guns.api.client.animation.gltf.accessor.Accessors;

public class AccessorModel {
   private final int componentType;
   private final ElementType elementType;
   private final int count;
   private int byteOffset;
   private BufferViewModel bufferViewModel;
   private int byteStride;
   private AccessorData accessorData;
   private Number[] max;
   private Number[] min;

   public AccessorModel(int componentType, int count, ElementType elementType) {
      this.componentType = componentType;
      this.count = count;
      this.elementType = elementType;
   }

   public BufferViewModel getBufferViewModel() {
      return this.bufferViewModel;
   }

   public void setBufferViewModel(BufferViewModel bufferViewModel) {
      this.bufferViewModel = bufferViewModel;
   }

   public int getComponentType() {
      return this.componentType;
   }

   public Class<?> getComponentDataType() {
      return Accessors.getDataTypeForAccessorComponentType(this.getComponentType());
   }

   public int getComponentSizeInBytes() {
      return Accessors.getNumBytesForAccessorComponentType(this.componentType);
   }

   public int getElementSizeInBytes() {
      return this.elementType.getNumComponents() * this.getComponentSizeInBytes();
   }

   public int getByteOffset() {
      return this.byteOffset;
   }

   public void setByteOffset(int byteOffset) {
      this.byteOffset = byteOffset;
   }

   public int getCount() {
      return this.count;
   }

   public ElementType getElementType() {
      return this.elementType;
   }

   public int getByteStride() {
      return this.byteStride;
   }

   public void setByteStride(int byteStride) {
      this.byteStride = byteStride;
   }

   public AccessorData getAccessorData() {
      if (this.accessorData == null) {
         this.accessorData = AccessorDatas.create(this);
      }

      return this.accessorData;
   }

   public Number[] getMin() {
      if (this.min == null) {
         this.min = AccessorDatas.computeMin(this.getAccessorData());
      }

      return (Number[])this.min.clone();
   }

   public Number[] getMax() {
      if (this.max == null) {
         this.max = AccessorDatas.computeMax(this.getAccessorData());
      }

      return (Number[])this.max.clone();
   }
}
