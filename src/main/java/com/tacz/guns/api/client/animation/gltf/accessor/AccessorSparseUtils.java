package com.tacz.guns.api.client.animation.gltf.accessor;

import java.util.logging.Logger;

public class AccessorSparseUtils {
   private static final Logger logger = Logger.getLogger(AccessorSparseUtils.class.getName());

   private AccessorSparseUtils() {
   }

   private static int[] extractIndices(AccessorData accessorData) {
      if (accessorData.getComponentType() == byte.class) {
         AccessorByteData accessorByteData = (AccessorByteData)accessorData;
         int numElements = accessorByteData.getNumElements();
         int[] indices = new int[numElements];

         for (int i = 0; i < numElements; i++) {
            indices[i] = accessorByteData.getInt(i, 0);
         }

         return indices;
      } else if (accessorData.getComponentType() == short.class) {
         AccessorShortData accessorShortData = (AccessorShortData)accessorData;
         int numElements = accessorShortData.getNumElements();
         int[] indices = new int[numElements];

         for (int i = 0; i < numElements; i++) {
            indices[i] = accessorShortData.getInt(i, 0);
         }

         return indices;
      } else {
         if (accessorData.getComponentType() != int.class) {
            throw new IllegalArgumentException("Invalid type for indices: " + accessorData.getComponentType());
         }

         AccessorIntData accessorIntData = (AccessorIntData)accessorData;
         int numElements = accessorIntData.getNumElements();
         int[] indices = new int[numElements];

         for (int i = 0; i < numElements; i++) {
            indices[i] = accessorIntData.get(i, 0);
         }

         return indices;
      }
   }

   public static void substituteAccessorData(
      AccessorData denseAccessorData, AccessorData baseAccessorData, AccessorData sparseIndicesAccessorData, AccessorData sparseValuesAccessorData
   ) {
      Class<?> componentType = denseAccessorData.getComponentType();
      if (componentType == byte.class) {
         AccessorByteData sparseValuesAccessorByteData = (AccessorByteData)sparseValuesAccessorData;
         AccessorByteData baseAccessorByteData = (AccessorByteData)baseAccessorData;
         AccessorByteData denseAccessorByteData = (AccessorByteData)denseAccessorData;
         substituteByteAccessorData(denseAccessorByteData, baseAccessorByteData, sparseIndicesAccessorData, sparseValuesAccessorByteData);
      } else if (componentType == short.class) {
         AccessorShortData sparseValuesAccessorShortData = (AccessorShortData)sparseValuesAccessorData;
         AccessorShortData baseAccessorShortData = (AccessorShortData)baseAccessorData;
         AccessorShortData denseAccessorShortData = (AccessorShortData)denseAccessorData;
         substituteShortAccessorData(denseAccessorShortData, baseAccessorShortData, sparseIndicesAccessorData, sparseValuesAccessorShortData);
      } else if (componentType == int.class) {
         AccessorIntData sparseValuesAccessorIntData = (AccessorIntData)sparseValuesAccessorData;
         AccessorIntData baseAccessorIntData = (AccessorIntData)baseAccessorData;
         AccessorIntData denseAccessorIntData = (AccessorIntData)denseAccessorData;
         substituteIntAccessorData(denseAccessorIntData, baseAccessorIntData, sparseIndicesAccessorData, sparseValuesAccessorIntData);
      } else if (componentType == float.class) {
         AccessorFloatData sparseValuesAccessorFloatData = (AccessorFloatData)sparseValuesAccessorData;
         AccessorFloatData baseAccessorFloatData = (AccessorFloatData)baseAccessorData;
         AccessorFloatData denseAccessorFloatData = (AccessorFloatData)denseAccessorData;
         substituteFloatAccessorData(denseAccessorFloatData, baseAccessorFloatData, sparseIndicesAccessorData, sparseValuesAccessorFloatData);
      } else {
         logger.warning("Invalid component type for accessor: " + componentType);
      }
   }

   private static void substituteByteAccessorData(
      AccessorByteData denseAccessorData, AccessorByteData baseAccessorData, AccessorData sparseIndicesAccessorData, AccessorByteData sparseValuesAccessorData
   ) {
      int numElements = denseAccessorData.getNumElements();
      int numComponentsPerElement = denseAccessorData.getNumComponentsPerElement();
      if (baseAccessorData != null) {
         for (int e = 0; e < numElements; e++) {
            for (int c = 0; c < numComponentsPerElement; c++) {
               byte value = baseAccessorData.get(e, c);
               denseAccessorData.set(e, c, value);
            }
         }
      }

      int[] indices = extractIndices(sparseIndicesAccessorData);

      for (int i = 0; i < indices.length; i++) {
         int targetElementIndex = indices[i];

         for (int c = 0; c < numComponentsPerElement; c++) {
            byte substitution = sparseValuesAccessorData.get(i, c);
            denseAccessorData.set(targetElementIndex, c, substitution);
         }
      }
   }

   private static void substituteShortAccessorData(
      AccessorShortData denseAccessorData,
      AccessorShortData baseAccessorData,
      AccessorData sparseIndicesAccessorData,
      AccessorShortData sparseValuesAccessorData
   ) {
      int numElements = denseAccessorData.getNumElements();
      int numComponentsPerElement = denseAccessorData.getNumComponentsPerElement();
      if (baseAccessorData != null) {
         for (int e = 0; e < numElements; e++) {
            for (int c = 0; c < numComponentsPerElement; c++) {
               short value = baseAccessorData.get(e, c);
               denseAccessorData.set(e, c, value);
            }
         }
      }

      int[] indices = extractIndices(sparseIndicesAccessorData);

      for (int i = 0; i < indices.length; i++) {
         int targetElementIndex = indices[i];

         for (int c = 0; c < numComponentsPerElement; c++) {
            short substitution = sparseValuesAccessorData.get(i, c);
            denseAccessorData.set(targetElementIndex, c, substitution);
         }
      }
   }

   private static void substituteIntAccessorData(
      AccessorIntData denseAccessorData, AccessorIntData baseAccessorData, AccessorData sparseIndicesAccessorData, AccessorIntData sparseValuesAccessorData
   ) {
      int numElements = denseAccessorData.getNumElements();
      int numComponentsPerElement = denseAccessorData.getNumComponentsPerElement();
      if (baseAccessorData != null) {
         for (int e = 0; e < numElements; e++) {
            for (int c = 0; c < numComponentsPerElement; c++) {
               int value = baseAccessorData.get(e, c);
               denseAccessorData.set(e, c, value);
            }
         }
      }

      int[] indices = extractIndices(sparseIndicesAccessorData);

      for (int i = 0; i < indices.length; i++) {
         int targetElementIndex = indices[i];

         for (int c = 0; c < numComponentsPerElement; c++) {
            int substitution = sparseValuesAccessorData.get(i, c);
            denseAccessorData.set(targetElementIndex, c, substitution);
         }
      }
   }

   private static void substituteFloatAccessorData(
      AccessorFloatData denseAccessorData,
      AccessorFloatData baseAccessorData,
      AccessorData sparseIndicesAccessorData,
      AccessorFloatData sparseValuesAccessorData
   ) {
      int numElements = denseAccessorData.getNumElements();
      int numComponentsPerElement = denseAccessorData.getNumComponentsPerElement();
      if (baseAccessorData != null) {
         for (int e = 0; e < numElements; e++) {
            for (int c = 0; c < numComponentsPerElement; c++) {
               float value = baseAccessorData.get(e, c);
               denseAccessorData.set(e, c, value);
            }
         }
      }

      int[] indices = extractIndices(sparseIndicesAccessorData);

      for (int i = 0; i < indices.length; i++) {
         int targetElementIndex = indices[i];

         for (int c = 0; c < numComponentsPerElement; c++) {
            float substitution = sparseValuesAccessorData.get(i, c);
            denseAccessorData.set(targetElementIndex, c, substitution);
         }
      }
   }
}
