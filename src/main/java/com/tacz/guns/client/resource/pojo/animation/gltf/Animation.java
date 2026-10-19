package com.tacz.guns.client.resource.pojo.animation.gltf;

import java.util.ArrayList;
import java.util.List;

public class Animation {
   private String name;
   private List<AnimationChannel> channels;
   private List<AnimationSampler> samplers;

   public List<AnimationChannel> getChannels() {
      return this.channels;
   }

   public void setChannels(List<AnimationChannel> channels) {
      if (channels == null) {
         throw new NullPointerException("Invalid value for channels: " + channels + ", may not be null");
      }

      if (channels.size() < 1) {
         throw new IllegalArgumentException("Number of channels elements is < 1");
      }

      this.channels = channels;
   }

   public void addChannels(AnimationChannel element) {
      if (element == null) {
         throw new NullPointerException("The element may not be null");
      }

      List<AnimationChannel> oldList = this.channels;
      List<AnimationChannel> newList = new ArrayList<>();
      if (oldList != null) {
         newList.addAll(oldList);
      }

      newList.add(element);
      this.channels = newList;
   }

   public void removeChannels(AnimationChannel element) {
      if (element == null) {
         throw new NullPointerException("The element may not be null");
      }

      List<AnimationChannel> oldList = this.channels;
      List<AnimationChannel> newList = new ArrayList<>();
      if (oldList != null) {
         newList.addAll(oldList);
      }

      newList.remove(element);
      this.channels = newList;
   }

   public List<AnimationSampler> getSamplers() {
      return this.samplers;
   }

   public void setSamplers(List<AnimationSampler> samplers) {
      if (samplers == null) {
         throw new NullPointerException("Invalid value for samplers: " + samplers + ", may not be null");
      }

      if (samplers.size() < 1) {
         throw new IllegalArgumentException("Number of samplers elements is < 1");
      }

      this.samplers = samplers;
   }

   public void addSamplers(AnimationSampler element) {
      if (element == null) {
         throw new NullPointerException("The element may not be null");
      }

      List<AnimationSampler> oldList = this.samplers;
      List<AnimationSampler> newList = new ArrayList<>();
      if (oldList != null) {
         newList.addAll(oldList);
      }

      newList.add(element);
      this.samplers = newList;
   }

   public void removeSamplers(AnimationSampler element) {
      if (element == null) {
         throw new NullPointerException("The element may not be null");
      }

      List<AnimationSampler> oldList = this.samplers;
      List<AnimationSampler> newList = new ArrayList<>();
      if (oldList != null) {
         newList.addAll(oldList);
      }

      newList.remove(element);
      this.samplers = newList;
   }

   public String getName() {
      return this.name;
   }

   public void setName(String name) {
      this.name = name;
   }
}
