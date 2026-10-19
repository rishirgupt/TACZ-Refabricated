package com.tacz.guns.api.client.animation.statemachine;

import com.tacz.guns.api.client.animation.AnimationController;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class AnimationStateMachine<T extends AnimationStateContext> {
   private List<AnimationState<T>> currentStates;
   protected T context;
   private Supplier<Iterable<? extends AnimationState<T>>> statesSupplier;
   @Nonnull
   private final AnimationController animationController;
   protected long exitingTime = -1L;

   public AnimationStateMachine(@Nonnull AnimationController animationController) {
      this.animationController = Objects.requireNonNull(animationController);
   }

   public void update() {
      if (this.context != null && this.currentStates != null) {
         this.currentStates.forEach(state -> state.update(this.context));
      }

      this.animationController.update();
   }

   public void visualUpdate() {
      if (this.context != null && this.currentStates != null) {
         this.currentStates.forEach(state -> state.update(this.context));
      }

      this.animationController.updateSoundOnly();
   }

   public void trigger(String condition) {
      if (this.context != null && this.currentStates != null) {
         ListIterator<AnimationState<T>> iterator = this.currentStates.listIterator();

         while (iterator.hasNext()) {
            AnimationState<T> state = iterator.next();
            AnimationState<T> nextState = state.transition(this.context, condition);
            if (nextState != null) {
               state.exitAction(this.context);
               iterator.set(nextState);
               nextState.entryAction(this.context);
            }
         }
      }
   }

   public void initialize() {
      if (this.context == null) {
         throw new IllegalStateException("Context must not be null before initialization");
      }

      if (this.currentStates != null) {
         throw new IllegalStateException("State machine is already initialized");
      }

      this.currentStates = new LinkedList<>();
      Optional.ofNullable(this.statesSupplier).map(Supplier::get).ifPresent(list -> list.forEach(state -> {
         this.currentStates.add((AnimationState<T>)state);
         state.entryAction(this.context);
      }));
   }

   public void exit() {
      this.checkNullPointer();
      this.currentStates.forEach(state -> state.exitAction(this.context));
      this.currentStates = null;
   }

   public void setExitingTime(long keepTime) {
      this.exitingTime = System.currentTimeMillis() + keepTime;
   }

   public long getExitingTime() {
      return this.exitingTime;
   }

   @Nonnull
   public AnimationController getAnimationController() {
      return this.animationController;
   }

   public boolean isInitialized() {
      return this.currentStates != null;
   }

   @Nullable
   public T getContext() {
      return this.context;
   }

   public void processContextIfExist(Consumer<T> consumer) {
      if (this.context != null) {
         consumer.accept(this.context);
      }
   }

   public void setContext(@Nonnull T context) {
      AnimationStateMachine<?> stateMachine = context.getStateMachine();
      if (stateMachine != null && stateMachine != this) {
         throw new IllegalStateException("Context is already used");
      }

      if (this.currentStates != null) {
         throw new IllegalStateException("State machine is already initialized, call exit() first");
      }

      if (this.context != null) {
         this.context.setStateMachine(null);
      }

      context.setStateMachine(this);
      this.context = context;
   }

   public void setStatesSupplier(Supplier<Iterable<? extends AnimationState<T>>> statesSupplier) {
      this.statesSupplier = statesSupplier;
   }

   private void checkNullPointer() {
      if (this.context == null) {
         throw new IllegalStateException("Context has not been initialized");
      }

      if (this.currentStates == null) {
         throw new IllegalStateException("State machine has not been initialized");
      }
   }
}
