package com.tacz.guns.api.vmlib;

import com.google.common.collect.Maps;
import com.tacz.guns.api.client.animation.ObjectAnimation;
import com.tacz.guns.api.client.animation.statemachine.AnimationConstant;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.Map.Entry;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;

public class LuaAnimationConstant implements LuaLibrary {
   private final Map<String, Object> constantMap = Maps.newHashMap();

   public LuaAnimationConstant() {
      Field[] fields = AnimationConstant.class.getFields();

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

      for (ObjectAnimation.PlayType playType : ObjectAnimation.PlayType.values()) {
         this.constantMap.put(playType.name(), playType.ordinal());
      }
   }

   @Override
   public void install(LuaValue chunk) {
      for (Entry<String, Object> entry : this.constantMap.entrySet()) {
         chunk.set(entry.getKey(), CoerceJavaToLua.coerce(entry.getValue()));
      }
   }
}
