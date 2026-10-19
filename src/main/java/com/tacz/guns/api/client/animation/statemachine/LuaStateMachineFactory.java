package com.tacz.guns.api.client.animation.statemachine;

import com.tacz.guns.api.client.animation.AnimationController;
import java.util.LinkedList;
import java.util.function.Supplier;
import org.luaj.vm2.LuaFunction;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;

public class LuaStateMachineFactory<T extends AnimationStateContext> {
   private AnimationController controller;
   private LuaFunction initializeFunc;
   private LuaFunction exitFunc;
   private LuaFunction statesFunc;
   private LuaTable table;

   public LuaAnimationStateMachine<T> build() {
      this.checkNullPointer();
      LuaAnimationStateMachine<T> stateMachine = new LuaAnimationStateMachine<>(this.controller);
      stateMachine.initializeFunc = context -> {
         if (this.initializeFunc != null) {
            this.initializeFunc.call(this.table, CoerceJavaToLua.coerce(context));
         }
      };
      stateMachine.exitFunc = context -> {
         if (this.exitFunc != null) {
            this.exitFunc.call(this.table, CoerceJavaToLua.coerce(context));
         }
      };
      stateMachine.setStatesSupplier(this.getStatesSupplier());
      return stateMachine;
   }

   public LuaStateMachineFactory<T> setController(AnimationController controller) {
      this.controller = controller;
      return this;
   }

   public LuaStateMachineFactory<T> setLuaScripts(LuaTable table) {
      if (table == null) {
         return this;
      }

      this.table = table;
      this.initializeFunc = this.checkFunction("initialize", table);
      this.exitFunc = this.checkFunction("exit", table);
      this.statesFunc = this.checkFunction("states", table);
      return this;
   }

   private LuaFunction checkFunction(String funcName, LuaTable table) {
      LuaValue value = table.get(funcName);
      return value.isnil() ? null : value.checkfunction();
   }

   private void checkNullPointer() {
      if (this.controller == null) {
         throw new IllegalStateException("controller must not be null before build");
      }
   }

   private Supplier<Iterable<? extends AnimationState<T>>> getStatesSupplier() {
      return this.statesFunc == null ? null : () -> {
         LuaTable statesTable = this.statesFunc.call(this.table).checktable();
         LinkedList<LuaAnimationState<T>> states = new LinkedList<>();

         for (int f = 1; f <= statesTable.length(); f++) {
            LuaTable stateTable = statesTable.get(f).checktable();
            states.add(new LuaAnimationState<>(stateTable, this.table));
         }

         return states;
      };
   }
}
