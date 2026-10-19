package com.tacz.guns.api.client.animation.gltf;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class AnimationModel {
   private final List<AnimationModel.Channel> channels = new ArrayList<>();
   private String name;

   public String getName() {
      return this.name;
   }

   public void setName(String name) {
      this.name = name;
   }

   public void addChannel(AnimationModel.Channel channel) {
      Objects.requireNonNull(channel, "The channel may not be null");
      this.channels.add(channel);
   }

   public List<AnimationModel.Channel> getChannels() {
      return Collections.unmodifiableList(this.channels);
   }

   public record Channel(AnimationModel.Sampler sampler, NodeModel nodeModel, String path) {
      public Channel(AnimationModel.Sampler sampler, NodeModel nodeModel, String path) {
         this.sampler = Objects.requireNonNull(sampler, "The sampler may not be null");
         this.nodeModel = nodeModel;
         this.path = Objects.requireNonNull(path, "The path may not be null");
      }
   }

   public enum Interpolation {
      STEP,
      LINEAR,
      SPLINE;
   }

   public record Sampler(AccessorModel input, AnimationModel.Interpolation interpolation, AccessorModel output) {
      public Sampler(AccessorModel input, AnimationModel.Interpolation interpolation, AccessorModel output) {
         this.input = Objects.requireNonNull(input, "The input may not be null");
         this.interpolation = Objects.requireNonNull(interpolation, "The interpolation may not be null");
         this.output = Objects.requireNonNull(output, "The output may not be null");
      }
   }
}
