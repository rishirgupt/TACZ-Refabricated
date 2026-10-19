package com.tacz.guns.api.client.animation.statemachine;

public interface AnimationState<T extends AnimationStateContext> {
   void update(T var1);

   void entryAction(T var1);

   void exitAction(T var1);

   AnimationState<T> transition(T var1, String var2);
}
