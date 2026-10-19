package com.tacz.guns.api.client.animation.statemachine;

import com.tacz.guns.api.client.animation.AnimationController;
import com.tacz.guns.api.client.animation.DiscreteTrackArray;
import com.tacz.guns.api.client.animation.ObjectAnimation;
import com.tacz.guns.api.client.animation.ObjectAnimationRunner;
import java.util.List;
import javax.annotation.Nonnull;
import org.jetbrains.annotations.Nullable;

public class AnimationStateContext {
   private boolean shouldHideCrossHair = false;
   @Nullable
   private AnimationStateMachine<?> stateMachine;
   private final DiscreteTrackArray trackArray = new DiscreteTrackArray();

   @Nullable
   public AnimationStateMachine<?> getStateMachine() {
      return this.stateMachine;
   }

   public DiscreteTrackArray getTrackArray() {
      return this.trackArray;
   }

   public int addTrackLine() {
      this.checkTrackArray();
      return this.getTrackArray().addTrackLine();
   }

   public void ensureTrackLineSize(int size) {
      this.checkTrackArray();
      this.getTrackArray().ensureCapacity(size);
   }

   public int getTrackLineSize() {
      this.checkTrackArray();
      return this.getTrackArray().getTrackLineSize();
   }

   public int assignNewTrack(int index) {
      this.checkTrackArray();
      return this.getTrackArray().assignNewTrack(index);
   }

   public int findIdleTrack(int index, boolean interruptHolding) {
      AnimationStateMachine<?> stateMachine = this.checkStateMachine();
      this.checkTrackArray();
      DiscreteTrackArray trackArray = this.getTrackArray();
      List<Integer> trackList = trackArray.getByIndex(index);
      AnimationController controller = stateMachine.getAnimationController();

      for (int track : trackList) {
         ObjectAnimationRunner animation = controller.getAnimation(track);
         if (animation == null || animation.isStopped() || interruptHolding && animation.isHolding()) {
            return track;
         }
      }

      return trackArray.assignNewTrack(index);
   }

   public void ensureTracksAmount(int index, int amount) {
      this.checkTrackArray();
      this.getTrackArray().ensureTrackAmount(index, amount);
   }

   public int getTrack(int trackLineIndex, int trackIndex) {
      this.checkTrackArray();
      DiscreteTrackArray trackArray = this.getTrackArray();
      if (trackLineIndex >= trackArray.getTrackLineSize()) {
         return -1;
      }

      List<Integer> tracks = trackArray.getByIndex(trackLineIndex);
      return trackIndex >= tracks.size() ? -1 : tracks.get(trackIndex);
   }

   public int getAsSingletonTrack(int index) {
      this.checkTrackArray();
      DiscreteTrackArray trackArray = this.getTrackArray();
      List<Integer> trackList = trackArray.getByIndex(index);
      return trackList.isEmpty() ? trackArray.assignNewTrack(index) : trackList.get(0);
   }

   public void runAnimation(String name, int track, boolean blending, int playType, float transitionTime) {
      AnimationStateMachine<?> stateMachine = this.checkStateMachine();
      ObjectAnimation.PlayType pt = ObjectAnimation.PlayType.values()[playType];
      stateMachine.getAnimationController().runAnimation(track, name, pt, transitionTime);
      stateMachine.getAnimationController().setBlending(track, blending);
   }

   public void stopAnimation(int track) {
      AnimationStateMachine<?> stateMachine = this.checkStateMachine();
      ObjectAnimationRunner runner = stateMachine.getAnimationController().getAnimation(track);
      if (runner != null) {
         runner.stop();
      }
   }

   public void holdAnimation(int track) {
      AnimationStateMachine<?> stateMachine = this.checkStateMachine();
      ObjectAnimationRunner runner = stateMachine.getAnimationController().getAnimation(track);
      if (runner != null) {
         runner.hold();
      }
   }

   public void pauseAnimation(int track) {
      AnimationStateMachine<?> stateMachine = this.checkStateMachine();
      ObjectAnimationRunner runner = stateMachine.getAnimationController().getAnimation(track);
      if (runner != null) {
         runner.pause();
      }
   }

   public void resumeAnimation(int track) {
      AnimationStateMachine<?> stateMachine = this.checkStateMachine();
      ObjectAnimationRunner runner = stateMachine.getAnimationController().getAnimation(track);
      if (runner != null) {
         runner.run();
      }
   }

   public void setAnimationProgress(int track, float progress, boolean normalization) {
      AnimationStateMachine<?> stateMachine = this.checkStateMachine();
      ObjectAnimationRunner runner = stateMachine.getAnimationController().getAnimation(track);
      if (runner != null) {
         if (runner.isRunning() || runner.isPausing()) {
            if (normalization) {
               progress = runner.getAnimation().getMaxEndTimeS() * progress;
            }

            runner.setProgressNs((long)(progress * 1.0E9));
            return;
         }

         ObjectAnimationRunner runner1 = runner.getTransitionTo();
         if (runner1 != null) {
            if (normalization) {
               progress = runner1.getAnimation().getMaxEndTimeS() * progress;
            }

            runner1.setProgressNs((long)(progress * 1.0E9));
         }
      }
   }

   public void adjustAnimationProgress(int track, float progress, boolean normalization) {
      AnimationStateMachine<?> stateMachine = this.checkStateMachine();
      ObjectAnimationRunner runner = stateMachine.getAnimationController().getAnimation(track);
      if (runner != null) {
         if (runner.isRunning()) {
            if (normalization) {
               progress = runner.getAnimation().getMaxEndTimeS() * progress;
            }

            runner.setProgressNs(runner.getProgressNs() + (long)(progress * 1.0E9));
            return;
         }

         ObjectAnimationRunner runner1 = runner.getTransitionTo();
         if (runner1 != null) {
            if (normalization) {
               progress = runner1.getAnimation().getMaxEndTimeS() * progress;
            }

            runner1.setProgressNs(runner1.getProgressNs() + (long)(progress * 1.0E9));
         }
      }
   }

   public boolean isHolding(int track) {
      AnimationStateMachine<?> stateMachine = this.checkStateMachine();
      ObjectAnimationRunner runner = stateMachine.getAnimationController().getAnimation(track);
      if (runner != null) {
         return runner.getTransitionTo() != null ? runner.getTransitionTo().isHolding() : runner.isHolding();
      } else {
         return false;
      }
   }

   public boolean isStopped(int track) {
      AnimationStateMachine<?> stateMachine = this.checkStateMachine();
      ObjectAnimationRunner runner = stateMachine.getAnimationController().getAnimation(track);
      if (runner != null) {
         return runner.getTransitionTo() != null ? runner.getTransitionTo().isStopped() : runner.isStopped();
      } else {
         return true;
      }
   }

   public boolean isPause(int track) {
      AnimationStateMachine<?> stateMachine = this.checkStateMachine();
      ObjectAnimationRunner runner = stateMachine.getAnimationController().getAnimation(track);
      if (runner != null) {
         return runner.getTransitionTo() != null ? !runner.getTransitionTo().isPausing() : !runner.isPausing();
      } else {
         return false;
      }
   }

   public boolean hasAnimationPrototype(String name) {
      AnimationStateMachine<?> stateMachine = this.checkStateMachine();
      AnimationController animationController = stateMachine.getAnimationController();
      return animationController.containPrototype(name);
   }

   public void trigger(String input) {
      AnimationStateMachine<?> stateMachine = this.checkStateMachine();
      stateMachine.trigger(input);
   }

   public boolean shouldHideCrossHair() {
      return this.shouldHideCrossHair;
   }

   public void setShouldHideCrossHair(boolean shouldHideCrossHair) {
      this.shouldHideCrossHair = shouldHideCrossHair;
   }

   void setStateMachine(@Nullable AnimationStateMachine<?> stateMachine) {
      if (this.stateMachine != null) {
         this.stateMachine.getAnimationController().setUpdatingTrackArray(null);
      }

      if (stateMachine != null) {
         stateMachine.getAnimationController().setUpdatingTrackArray(this.trackArray);
      }

      this.stateMachine = stateMachine;
   }

   private void checkTrackArray() {
      if (this.stateMachine != null && this.stateMachine.getAnimationController().getUpdatingTrackArray() != this.trackArray) {
         throw new TrackArrayMismatchException();
      }
   }

   @Nonnull
   private AnimationStateMachine<?> checkStateMachine() {
      if (this.stateMachine == null) {
         throw new IllegalStateException("This context has not been bound to a state machine.");
      } else {
         return this.stateMachine;
      }
   }
}
