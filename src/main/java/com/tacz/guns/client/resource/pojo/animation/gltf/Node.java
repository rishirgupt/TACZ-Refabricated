package com.tacz.guns.client.resource.pojo.animation.gltf;

import java.util.ArrayList;
import java.util.List;

public class Node {
   private String name;
   private List<Integer> children;
   private float[] matrix;
   private float[] rotation;
   private float[] scale;
   private float[] translation;

   public List<Integer> getChildren() {
      return this.children;
   }

   public void setChildren(List<Integer> children) {
      if (children == null) {
         this.children = children;
      } else {
         if (children.size() < 1) {
            throw new IllegalArgumentException("Number of children elements is < 1");
         }

         for (Integer childrenElement : children) {
            if (childrenElement < 0) {
               throw new IllegalArgumentException("childrenElement < 0");
            }
         }

         this.children = children;
      }
   }

   public void addChildren(Integer element) {
      if (element == null) {
         throw new NullPointerException("The element may not be null");
      }

      List<Integer> oldList = this.children;
      List<Integer> newList = new ArrayList<>();
      if (oldList != null) {
         newList.addAll(oldList);
      }

      newList.add(element);
      this.children = newList;
   }

   public void removeChildren(Integer element) {
      if (element == null) {
         throw new NullPointerException("The element may not be null");
      }

      List<Integer> oldList = this.children;
      List<Integer> newList = new ArrayList<>();
      if (oldList != null) {
         newList.addAll(oldList);
      }

      newList.remove(element);
      if (newList.isEmpty()) {
         this.children = null;
      } else {
         this.children = newList;
      }
   }

   public float[] getMatrix() {
      return this.matrix;
   }

   public void setMatrix(float[] matrix) {
      if (matrix == null) {
         this.matrix = matrix;
      } else {
         if (matrix.length < 16) {
            throw new IllegalArgumentException("Number of matrix elements is < 16");
         }

         if (matrix.length > 16) {
            throw new IllegalArgumentException("Number of matrix elements is > 16");
         }

         this.matrix = matrix;
      }
   }

   public float[] getRotation() {
      return this.rotation;
   }

   public void setRotation(float[] rotation) {
      if (rotation == null) {
         this.rotation = rotation;
      } else {
         if (rotation.length < 4) {
            throw new IllegalArgumentException("Number of rotation elements is < 4");
         }

         if (rotation.length > 4) {
            throw new IllegalArgumentException("Number of rotation elements is > 4");
         }

         for (float rotationElement : rotation) {
            if (rotationElement > 1.0) {
               throw new IllegalArgumentException("rotationElement > 1.0");
            }

            if (rotationElement < -1.0) {
               throw new IllegalArgumentException("rotationElement < -1.0");
            }
         }

         this.rotation = rotation;
      }
   }

   public float[] getScale() {
      return this.scale;
   }

   public void setScale(float[] scale) {
      if (scale == null) {
         this.scale = scale;
      } else {
         if (scale.length < 3) {
            throw new IllegalArgumentException("Number of scale elements is < 3");
         }

         if (scale.length > 3) {
            throw new IllegalArgumentException("Number of scale elements is > 3");
         }

         this.scale = scale;
      }
   }

   public float[] getTranslation() {
      return this.translation;
   }

   public void setTranslation(float[] translation) {
      if (translation == null) {
         this.translation = translation;
      } else {
         if (translation.length < 3) {
            throw new IllegalArgumentException("Number of translation elements is < 3");
         }

         if (translation.length > 3) {
            throw new IllegalArgumentException("Number of translation elements is > 3");
         }

         this.translation = translation;
      }
   }

   public String getName() {
      return this.name;
   }

   public void setName(String name) {
      this.name = name;
   }
}
