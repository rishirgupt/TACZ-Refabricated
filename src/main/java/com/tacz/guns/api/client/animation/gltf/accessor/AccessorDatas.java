package com.tacz.guns.api.client.animation.gltf.accessor;

import com.tacz.guns.api.client.animation.gltf.AccessorModel;
import com.tacz.guns.api.client.animation.gltf.BufferViewModel;
import com.tacz.guns.api.client.animation.gltf.GltfConstants;
import java.nio.ByteBuffer;
import java.util.Locale;

public class AccessorDatas {
   private AccessorDatas() {
   }

   public static AccessorData create(AccessorModel accessorModel) {
      BufferViewModel bufferViewModel = accessorModel.getBufferViewModel();
      ByteBuffer bufferViewData = bufferViewModel.getBufferViewData();
      return create(accessorModel, bufferViewData);
   }

   public static AccessorData create(AccessorModel accessorModel, ByteBuffer byteBuffer) {
      if (accessorModel.getComponentDataType() == byte.class) {
         return createByte(accessorModel, byteBuffer);
      } else if (accessorModel.getComponentDataType() == short.class) {
         return createShort(accessorModel, byteBuffer);
      } else if (accessorModel.getComponentDataType() == int.class) {
         return createInt(accessorModel, byteBuffer);
      } else {
         return accessorModel.getComponentDataType() == float.class ? createFloat(accessorModel, byteBuffer) : null;
      }
   }

   public static AccessorData create(int componentType, ByteBuffer bufferViewData, int byteOffset, int count, int numComponentsPerElement, Integer byteStride) {
      if (isByteType(componentType)) {
         return new AccessorByteData(componentType, bufferViewData, byteOffset, count, numComponentsPerElement, byteStride);
      } else if (isShortType(componentType)) {
         return new AccessorShortData(componentType, bufferViewData, byteOffset, count, numComponentsPerElement, byteStride);
      } else if (isIntType(componentType)) {
         return new AccessorIntData(componentType, bufferViewData, byteOffset, count, numComponentsPerElement, byteStride);
      } else if (isFloatType(componentType)) {
         return new AccessorFloatData(componentType, bufferViewData, byteOffset, count, numComponentsPerElement, byteStride);
      } else {
         throw new IllegalArgumentException("Not a valid component type: " + componentType);
      }
   }

   public static boolean isByteType(int type) {
      return type == 5120 || type == 5121;
   }

   public static boolean isShortType(int type) {
      return type == 5122 || type == 5123;
   }

   public static boolean isIntType(int type) {
      return type == 5124 || type == 5125;
   }

   public static boolean isFloatType(int type) {
      return type == 5126;
   }

   static boolean isUnsignedType(int type) {
      return type == 5121 || type == 5123 || type == 5125;
   }

   static void validateByteType(int type) {
      if (!isByteType(type)) {
         throw new IllegalArgumentException("The type is not GL_BYTE or GL_UNSIGNED_BYTE, but " + GltfConstants.stringFor(type));
      }
   }

   static void validateShortType(int type) {
      if (!isShortType(type)) {
         throw new IllegalArgumentException("The type is not GL_SHORT or GL_UNSIGNED_SHORT, but " + GltfConstants.stringFor(type));
      }
   }

   static void validateIntType(int type) {
      if (!isIntType(type)) {
         throw new IllegalArgumentException("The type is not GL_INT or GL_UNSIGNED_INT, but " + GltfConstants.stringFor(type));
      }
   }

   static void validateFloatType(int type) {
      if (!isFloatType(type)) {
         throw new IllegalArgumentException("The type is not GL_FLOAT, but " + GltfConstants.stringFor(type));
      }
   }

   static AccessorByteData createByte(AccessorModel accessorModel) {
      BufferViewModel bufferViewModel = accessorModel.getBufferViewModel();
      return createByte(accessorModel, bufferViewModel.getBufferViewData());
   }

   private static AccessorByteData createByte(AccessorModel accessorModel, ByteBuffer bufferViewByteBuffer) {
      return new AccessorByteData(
         accessorModel.getComponentType(),
         bufferViewByteBuffer,
         accessorModel.getByteOffset(),
         accessorModel.getCount(),
         accessorModel.getElementType().getNumComponents(),
         accessorModel.getByteStride()
      );
   }

   static AccessorShortData createShort(AccessorModel accessorModel) {
      BufferViewModel bufferViewModel = accessorModel.getBufferViewModel();
      return createShort(accessorModel, bufferViewModel.getBufferViewData());
   }

   private static AccessorShortData createShort(AccessorModel accessorModel, ByteBuffer bufferViewByteBuffer) {
      return new AccessorShortData(
         accessorModel.getComponentType(),
         bufferViewByteBuffer,
         accessorModel.getByteOffset(),
         accessorModel.getCount(),
         accessorModel.getElementType().getNumComponents(),
         accessorModel.getByteStride()
      );
   }

   static AccessorIntData createInt(AccessorModel accessorModel) {
      BufferViewModel bufferViewModel = accessorModel.getBufferViewModel();
      return createInt(accessorModel, bufferViewModel.getBufferViewData());
   }

   private static AccessorIntData createInt(AccessorModel accessorModel, ByteBuffer bufferViewByteBuffer) {
      return new AccessorIntData(
         accessorModel.getComponentType(),
         bufferViewByteBuffer,
         accessorModel.getByteOffset(),
         accessorModel.getCount(),
         accessorModel.getElementType().getNumComponents(),
         accessorModel.getByteStride()
      );
   }

   public static AccessorFloatData createFloat(AccessorModel accessorModel) {
      BufferViewModel bufferViewModel = accessorModel.getBufferViewModel();
      return createFloat(accessorModel, bufferViewModel.getBufferViewData());
   }

   private static AccessorFloatData createFloat(AccessorModel accessorModel, ByteBuffer bufferViewByteBuffer) {
      return new AccessorFloatData(
         accessorModel.getComponentType(),
         bufferViewByteBuffer,
         accessorModel.getByteOffset(),
         accessorModel.getCount(),
         accessorModel.getElementType().getNumComponents(),
         accessorModel.getByteStride()
      );
   }

   static void validateCapacity(int byteOffset, int numElements, int byteStridePerElement, int bufferCapacity) {
      int expectedCapacity = numElements * byteStridePerElement;
      if (expectedCapacity > bufferCapacity) {
         throw new IllegalArgumentException(
            "The accessorModel has an offset of "
               + byteOffset
               + " and "
               + numElements
               + " elements with a byte stride of "
               + byteStridePerElement
               + ", requiring "
               + expectedCapacity
               + " bytes, but the buffer view has only "
               + bufferCapacity
               + " bytes"
         );
      }
   }

   public static Number[] computeMin(AccessorData accessorData) {
      if (accessorData instanceof AccessorByteData accessorByteData) {
         return NumberArrays.asNumbers(accessorByteData.computeMinInt());
      } else if (accessorData instanceof AccessorShortData accessorShortData) {
         return NumberArrays.asNumbers(accessorShortData.computeMinInt());
      } else if (accessorData instanceof AccessorIntData accessorIntData) {
         return NumberArrays.asNumbers(accessorIntData.computeMinLong());
      } else if (accessorData instanceof AccessorFloatData accessorFloatData) {
         return NumberArrays.asNumbers(accessorFloatData.computeMin());
      } else {
         throw new IllegalArgumentException("Invalid data type: " + accessorData);
      }
   }

   public static Number[] computeMax(AccessorData accessorData) {
      if (accessorData instanceof AccessorByteData accessorByteData) {
         return NumberArrays.asNumbers(accessorByteData.computeMaxInt());
      } else if (accessorData instanceof AccessorShortData accessorShortData) {
         return NumberArrays.asNumbers(accessorShortData.computeMaxInt());
      } else if (accessorData instanceof AccessorIntData accessorIntData) {
         return NumberArrays.asNumbers(accessorIntData.computeMaxLong());
      } else if (accessorData instanceof AccessorFloatData accessorFloatData) {
         return NumberArrays.asNumbers(accessorFloatData.computeMax());
      } else {
         throw new IllegalArgumentException("Invalid data type: " + accessorData);
      }
   }

   public static String createString(AccessorData accessorData, int elementsPerRow) {
      if (accessorData instanceof AccessorByteData accessorByteData) {
         return accessorByteData.createString(Locale.ENGLISH, "%4d", elementsPerRow);
      } else if (accessorData instanceof AccessorShortData accessorShortData) {
         return accessorShortData.createString(Locale.ENGLISH, "%6d", elementsPerRow);
      } else if (accessorData instanceof AccessorIntData accessorIntData) {
         return accessorIntData.createString(Locale.ENGLISH, "%11d", elementsPerRow);
      } else {
         return accessorData instanceof AccessorFloatData accessorFloatData
            ? accessorFloatData.createString(Locale.ENGLISH, "%10.5f", elementsPerRow)
            : "Unknown accessor data type: " + accessorData;
      }
   }
}
