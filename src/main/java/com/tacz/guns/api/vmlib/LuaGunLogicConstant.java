package com.tacz.guns.api.vmlib;

import com.google.common.collect.Maps;
import com.tacz.guns.api.entity.ReloadState;
import com.tacz.guns.api.item.gun.FireMode;
import java.util.Map;
import java.util.Map.Entry;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;

public class LuaGunLogicConstant implements LuaLibrary {
   private final Map<String, Object> constantMap = Maps.newHashMap();

   public LuaGunLogicConstant() {
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
