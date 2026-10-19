package com.tacz.guns.client.resource.pojo.animation.gltf;

public class AnimationChannelTarget {
   private Integer node;
   private String path;

   public Integer getNode() {
      return this.node;
   }

   public void setNode(Integer node) {
      if (node == null) {
         this.node = node;
      } else {
         this.node = node;
      }
   }

   public String getPath() {
      return this.path;
   }

   public void setPath(String path) {
      if (path == null) {
         throw new NullPointerException("Invalid value for path: " + path + ", may not be null");
      }

      if (!"translation".equals(path) && !"rotation".equals(path) && !"scale".equals(path) && !"weights".equals(path)) {
         throw new IllegalArgumentException("Invalid value for path: " + path + ", valid: [translation, rotation, scale, weights]");
      }

      this.path = path;
   }
}
