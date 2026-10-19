package com.tacz.guns.resource.manager;

import com.tacz.guns.GunMod;
import java.io.IOException;
import java.io.StringReader;
import java.util.Locale;
import javax.annotation.Nullable;
import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaClosure;
import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaNumber;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Prototype;
import org.luaj.vm2.compiler.LuaC;
import org.luaj.vm2.lib.BaseLib;
import org.luaj.vm2.lib.Bit32Lib;
import org.luaj.vm2.lib.TableLib;
import org.luaj.vm2.lib.jse.JseMathLib;
import org.luaj.vm2.lib.jse.JseStringLib;

public final class AttachmentScriptManager {
   private static final String[] UNSAFE_GLOBALS = new String[]{"collectgarbage", "dofile", "loadfile", "load", "print", "getmetatable"};
   private static final Globals GLOBALS = secureStandardGlobals();

   private AttachmentScriptManager() {
   }

   @Nullable
   public static AttachmentScriptManager.CompiledScript compile(@Nullable String script) {
      if (script != null && !script.isEmpty()) {
         try {
            Prototype prototype = GLOBALS.compilePrototype(new StringReader(script.toLowerCase(Locale.ENGLISH)), "attachment_modifier");
            return new AttachmentScriptManager.CompiledScript(prototype);
         } catch (IOException | LuaError e) {
            GunMod.LOGGER.warn("Failed to compile attachment modifier function", e);
            return null;
         }
      } else {
         return null;
      }
   }

   private static Globals secureStandardGlobals() {
      Globals globals = new Globals();
      globals.load(new BaseLib());
      globals.load(new Bit32Lib());
      globals.load(new TableLib());
      globals.load(new JseStringLib());
      globals.load(new JseMathLib());

      for (String name : UNSAFE_GLOBALS) {
         globals.set(name, LuaValue.NIL);
      }

      LuaC.install(globals);
      return globals;
   }

   public static final class CompiledScript {
      private final LuaClosure function;

      private CompiledScript(Prototype prototype) {
         this.function = new LuaClosure(prototype, AttachmentScriptManager.GLOBALS);
      }

      public double eval(double value, double defaultValue) {
         synchronized (AttachmentScriptManager.GLOBALS) {
            AttachmentScriptManager.GLOBALS.rawset("x", LuaValue.valueOf(value));
            AttachmentScriptManager.GLOBALS.rawset("r", LuaValue.valueOf(defaultValue));
            AttachmentScriptManager.GLOBALS.rawset("y", LuaValue.NIL);

            double var10000;
            try {
               this.function.call();
               LuaValue result = AttachmentScriptManager.GLOBALS.rawget("y");
               var10000 = result instanceof LuaNumber ? result.todouble() : value;
            } catch (LuaError e) {
               GunMod.LOGGER.warn("Failed to execute attachment modifier function", e);
               return value;
            }

            return var10000;
         }
      }
   }
}
