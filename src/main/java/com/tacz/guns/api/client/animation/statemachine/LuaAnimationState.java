package com.tacz.guns.api.client.animation.statemachine;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaFunction;
import org.luaj.vm2.LuaString;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;

public class LuaAnimationState<T extends AnimationStateContext> implements AnimationState<T> {
   @Nonnull
   private final LuaTable stateTable;
   @Nonnull
   private final LuaTable scriptTable;
   @Nullable
   private final LuaFunction updateFunction;
   @Nullable
   private final LuaFunction enterFunction;
   @Nullable
   private final LuaFunction exitFunction;
   @Nullable
   private final LuaFunction transitionFunction;

   LuaAnimationState(@Nonnull LuaTable stateTable, @Nonnull LuaTable scriptTable) {
      this.stateTable = stateTable;
      this.scriptTable = scriptTable;
      this.updateFunction = this.checkLuaFunction("update");
      this.enterFunction = this.checkLuaFunction("entry");
      this.exitFunction = this.checkLuaFunction("exit");
      this.transitionFunction = this.checkLuaFunction("transition");
   }

   @Override
   public void update(T context) {
      if (this.updateFunction != null) {
         this.updateFunction.call(this.scriptTable, CoerceJavaToLua.coerce(context));
      }
   }

   @Override
   public void entryAction(T context) {
      if (this.enterFunction != null) {
         this.enterFunction.call(this.scriptTable, CoerceJavaToLua.coerce(context));
      }
   }

   @Override
   public void exitAction(T context) {
      if (this.exitFunction != null) {
         this.exitFunction.call(this.scriptTable, CoerceJavaToLua.coerce(context));
      }
   }

   @Override
   public AnimationState<T> transition(T context, String condition) {
      if (this.transitionFunction != null) {
         LuaString conditionToLua = LuaString.valueOf(condition);
         LuaValue nextStateTable = this.transitionFunction.call(this.scriptTable, CoerceJavaToLua.coerce(context), conditionToLua);
         if (nextStateTable.istable()) {
            return new LuaAnimationState<>((LuaTable)nextStateTable, this.scriptTable);
         } else if (nextStateTable.isnil()) {
            return null;
         } else {
            throw new LuaError("the return of function 'transition' must be table or nil");
         }
      } else {
         return null;
      }
   }

   private LuaFunction checkLuaFunction(String funcName) {
      LuaValue value = this.stateTable.get(funcName);
      if (value.isfunction()) {
         return (LuaFunction)value;
      } else if (value.isnil()) {
         return null;
      } else {
         throw new LuaError("the type of field '" + funcName + "' must be function or nil");
      }
   }
}
