package com.tacz.guns.api.client.animation.statemachine;

import com.tacz.guns.api.client.animation.AnimationController;
import java.util.function.Consumer;

public class LuaAnimationStateMachine<T extends AnimationStateContext> extends AnimationStateMachine<T> {
   Consumer<T> initializeFunc;
   Consumer<T> exitFunc;

   LuaAnimationStateMachine(AnimationController animationController) {
      super(animationController);
   }

   @Override
   public void initialize() {
      this.initializeFunc.accept(this.context);
      super.initialize();
   }

   @Override
   public void exit() {
      this.exitFunc.accept(this.context);
      super.exit();
   }
}
