package com.tacz.guns.client.resource.pojo.animation.gltf;

import java.util.List;

public class RawAnimationStructure {
   private List<Accessor> accessors;
   private List<Animation> animations;
   private List<Buffer> buffers;
   private List<BufferView> bufferViews;
   private List<Node> nodes;

   public List<Accessor> getAccessors() {
      return this.accessors;
   }

   public void setAccessors(List<Accessor> accessors) {
      this.accessors = accessors;
   }

   public List<Animation> getAnimations() {
      return this.animations;
   }

   public void setAnimations(List<Animation> animations) {
      this.animations = animations;
   }

   public List<Buffer> getBuffers() {
      return this.buffers;
   }

   public void setBuffers(List<Buffer> buffers) {
      this.buffers = buffers;
   }

   public List<BufferView> getBufferViews() {
      return this.bufferViews;
   }

   public void setBufferViews(List<BufferView> bufferViews) {
      this.bufferViews = bufferViews;
   }

   public List<Node> getNodes() {
      return this.nodes;
   }

   public void setNodes(List<Node> nodes) {
      this.nodes = nodes;
   }
}
