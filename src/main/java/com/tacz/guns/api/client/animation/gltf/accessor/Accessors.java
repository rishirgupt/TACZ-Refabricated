package com.tacz.guns.api.client.animation.gltf.accessor;

public class Accessors {
   private Accessors() {
   }

   public static int getNumComponentsForAccessorType(String accessorType) {
      switch (accessorType) {
         case "SCALAR":
            return 1;
         case "VEC2":
            return 2;
         case "VEC3":
            return 3;
         case "VEC4":
            return 4;
         case "MAT2":
            return 4;
         case "MAT3":
            return 9;
         case "MAT4":
            return 16;
         default:
            throw new IllegalArgumentException("Invalid accessor type: " + accessorType);
      }
   }

   public static int getNumBytesForAccessorComponentType(int componentType) {
      switch (componentType) {
         case 5120:
            return 1;
         case 5121:
            return 1;
         case 5122:
            return 2;
         case 5123:
            return 2;
         case 5124:
            return 4;
         case 5125:
            return 4;
         case 5126:
            return 4;
         default:
            throw new IllegalArgumentException("Invalid accessor component type: " + componentType);
      }
   }

   public static Class<?> getDataTypeForAccessorComponentType(int componentType) {
      switch (componentType) {
         case 5120:
            return byte.class;
         case 5121:
            return byte.class;
         case 5122:
            return short.class;
         case 5123:
            return short.class;
         case 5124:
            return int.class;
         case 5125:
            return int.class;
         case 5126:
            return float.class;
         default:
            throw new IllegalArgumentException("Invalid accessor component type: " + componentType);
      }
   }
}
