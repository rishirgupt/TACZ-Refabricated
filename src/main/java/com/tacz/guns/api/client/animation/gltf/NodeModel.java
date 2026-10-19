package com.tacz.guns.api.client.animation.gltf;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class NodeModel {
   private final List<NodeModel> children = new ArrayList<>();
   private String name;
   private float[] matrix;
   private float[] translation;
   private float[] rotation;
   private float[] scale;
   private NodeModel parent;

   private static float[] check(float[] array, int expectedLength) {
      if (array == null) {
         return null;
      } else if (array.length != expectedLength) {
         throw new IllegalArgumentException("Expected " + expectedLength + " array elements, but found " + array.length);
      } else {
         return array;
      }
   }

   public String getName() {
      return this.name;
   }

   public void setName(String name) {
      this.name = name;
   }

   public NodeModel getParent() {
      return this.parent;
   }

   public List<NodeModel> getChildren() {
      return Collections.unmodifiableList(this.children);
   }

   public void addChild(NodeModel child) {
      Objects.requireNonNull(child, "The child may not be null");
      this.children.add(child);
      child.parent = this;
   }

   public float[] getMatrix() {
      return this.matrix;
   }

   public void setMatrix(float[] matrix) {
      this.matrix = check(matrix, 16);
   }

   public float[] getTranslation() {
      return this.translation;
   }

   public void setTranslation(float[] translation) {
      this.translation = check(translation, 3);
   }

   public float[] getRotation() {
      return this.rotation;
   }

   public void setRotation(float[] rotation) {
      this.rotation = check(rotation, 4);
   }

   public float[] getScale() {
      return this.scale;
   }

   public void setScale(float[] scale) {
      this.scale = check(scale, 3);
   }
}
