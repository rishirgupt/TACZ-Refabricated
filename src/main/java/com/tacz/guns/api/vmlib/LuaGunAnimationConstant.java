package com.tacz.guns.api.vmlib;

import com.google.common.collect.Maps;
import com.tacz.guns.api.entity.ReloadState;
import com.tacz.guns.api.item.gun.FireMode;
import com.tacz.guns.client.animation.statemachine.GunAnimationConstant;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.Map.Entry;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;

public class LuaGunAnimationConstant implements LuaLibrary {
   private final Map<String, Object> constantMap = Maps.newHashMap();

   public LuaGunAnimationConstant() {
      Field[] fields = GunAnimationConstant.class.getFields();

      for (Field field : fields) {
         if (Modifier.isStatic(field.getModifiers()) && Modifier.isFinal(field.getModifiers())) {
            try {
               String name = field.getName();
               Object value = field.get(null);
               this.constantMap.put(name, value);
            } catch (IllegalAccessException e) {
               e.printStackTrace();
            }
         }
      }

      for (ReloadState.StateType stateType : ReloadState.StateType.values()) {
         this.constantMap.put(stateType.name(), stateType.ordinal());
      }

      for (FireMode fireMode : FireMode.values()) {
         this.constantMap.put(fireMode.name(), fireMode.ordinal());
      }
   }

   @Override
   public void install(LuaValue chunk) {
      for (Entry<String, Object> entry : this.constantMap.entrySet()) {
         chunk.set(entry.getKey(), CoerceJavaToLua.coerce(entry.getValue()));
      }
   }
}
