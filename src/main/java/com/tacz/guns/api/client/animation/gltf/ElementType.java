package com.tacz.guns.api.client.animation.gltf;

public enum ElementType {
   SCALAR(1),
   VEC2(2),
   VEC3(3),
   VEC4(4),
   MAT2(4),
   MAT3(9),
   MAT4(16);

   private final int numComponents;

   ElementType(int numComponents) {
      this.numComponents = numComponents;
   }

   public static boolean contains(String s) {
      for (ElementType elementType : values()) {
         if (elementType.name().equals(s)) {
            return true;
         }
      }

      return false;
   }

   public static ElementType forString(String string) {
      if (string == null) {
         return null;
      } else {
         return !contains(string) ? null : valueOf(string);
      }
   }

   public int getNumComponents() {
      return this.numComponents;
   }
}
