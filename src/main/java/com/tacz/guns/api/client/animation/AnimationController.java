package com.tacz.guns.api.client.animation;

import com.google.common.collect.Maps;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.function.Supplier;
import javax.annotation.Nullable;

public class AnimationController {
   protected final ArrayList<ObjectAnimationRunner> currentRunners = new ArrayList<>();
   protected final ArrayList<Boolean> blending = new ArrayList<>();
   private final AnimationListenerSupplier listenerSupplier;
   private final ArrayList<Queue<AnimationPlan>> animationQueue = new ArrayList<>();
   protected Map<String, ObjectAnimation> prototypes = Maps.newHashMap();
   @Nullable
   protected Iterable<Integer> updatingTrackArray = null;

   public AnimationController(List<ObjectAnimation> animationPrototypes, AnimationListenerSupplier model) {
      for (ObjectAnimation prototype : animationPrototypes) {
         if (prototype != null) {
            this.prototypes.put(prototype.name, prototype);
         }
      }

      this.listenerSupplier = model;
   }

   public void providePrototypeIfAbsent(String name, Supplier<ObjectAnimation> supplier) {
      if (!this.prototypes.containsKey(name)) {
         this.prototypes.put(name, supplier.get());
      }
   }

   public boolean containPrototype(String name) {
      return this.prototypes.containsKey(name);
   }

   @Nullable
   public ObjectAnimationRunner getAnimation(int track) {
      return track >= this.currentRunners.size() ? null : this.currentRunners.get(track);
   }

   public void removeAnimation(int track) {
      if (track < this.currentRunners.size()) {
         this.currentRunners.set(track, null);
      }

      if (track < this.animationQueue.size()) {
         this.animationQueue.set(track, null);
      }
   }

   public void queueAnimation(int track, Queue<AnimationPlan> queue) {
      for (int i = this.animationQueue.size(); i <= track; i++) {
         this.animationQueue.add(null);
      }

      this.animationQueue.set(track, queue);
      if (queue != null) {
         AnimationPlan plan = null;

         while (plan == null && !queue.isEmpty()) {
            plan = queue.poll();
         }

         if (plan != null) {
            this.run(track, plan.animationName, plan.playType, plan.transitionTimeS);
         }
      }
   }

   public void runAnimation(int track, String animationName, ObjectAnimation.PlayType playType, float transitionTimeS) {
      if (track < this.animationQueue.size()) {
         this.animationQueue.set(track, null);
      }

      this.run(track, animationName, playType, transitionTimeS);
   }

   private synchronized void run(int track, String animationName, ObjectAnimation.PlayType playType, float transitionTimeS) {
      ObjectAnimation prototype = this.prototypes.get(animationName);
      if (prototype != null) {
         for (int i = this.currentRunners.size(); i <= track; i++) {
            this.currentRunners.add(null);
         }

         ObjectAnimation animation = new ObjectAnimation(prototype);
         animation.applyAnimationListeners(this.listenerSupplier);
         animation.playType = playType;
         ObjectAnimationRunner runner = new ObjectAnimationRunner(animation);
         runner.setProgressNs(0L);
         runner.run();
         ObjectAnimationRunner oldRunner = this.currentRunners.get(track);
         if (transitionTimeS > 0.0F) {
            if (oldRunner != null) {
               oldRunner.transition(runner, (long)(transitionTimeS * 1.0E9));
            } else {
               this.currentRunners.set(track, runner);
            }
         } else {
            this.currentRunners.set(track, runner);
         }
      }
   }

   public void setBlending(int track, boolean blend) {
      for (int i = this.blending.size(); i <= track; i++) {
         this.blending.add(false);
      }

      this.blending.set(track, blend);
   }

   @Nullable
   public Iterable<Integer> getUpdatingTrackArray() {
      return this.updatingTrackArray;
   }

   public void setUpdatingTrackArray(@Nullable Iterable<Integer> updatingTrackArray) {
      this.updatingTrackArray = updatingTrackArray;
   }

   public synchronized void update() {
      if (this.updatingTrackArray != null) {
         this.updatingTrackArray.forEach(track -> this.updateByTrack(track, false));
      } else {
         for (int i = 0; i < this.currentRunners.size(); i++) {
            this.updateByTrack(i, false);
         }
      }
   }

   public synchronized void updateSoundOnly() {
      for (int i = 0; i < this.currentRunners.size(); i++) {
         this.updateByTrack(i, true);
      }
   }

   private void updateByTrack(int track, boolean isSoundOnly) {
      if (track < this.currentRunners.size()) {
         boolean blend = track < this.blending.size() ? this.blending.get(track) : false;
         ObjectAnimationRunner runner = this.currentRunners.get(track);
         if (runner != null) {
            if (runner.isRunning() || runner.isHolding() || runner.isPausing() || runner.isTransitioning()) {
               if (isSoundOnly) {
                  runner.updateSoundOnly();
               } else {
                  runner.update(blend);
               }
            }

            if (runner.getTransitionTo() != null) {
               if (isSoundOnly) {
                  runner.getTransitionTo().updateSoundOnly();
               } else {
                  runner.getTransitionTo().update(blend);
               }

               if (!runner.isTransitioning()) {
                  this.currentRunners.set(track, runner.getTransitionTo());
                  runner = runner.getTransitionTo();
               }
            }

            if ((runner.isHolding() || runner.isStopped()) && !runner.isTransitioning() && track < this.animationQueue.size()) {
               Queue<AnimationPlan> queue = this.animationQueue.get(track);
               if (queue != null) {
                  AnimationPlan plan = null;

                  while (plan == null && !queue.isEmpty()) {
                     plan = queue.poll();
                  }

                  if (plan != null) {
                     this.run(track, plan.animationName, plan.playType, plan.transitionTimeS);
                  }
               }
            }
         }
      }
   }
}
