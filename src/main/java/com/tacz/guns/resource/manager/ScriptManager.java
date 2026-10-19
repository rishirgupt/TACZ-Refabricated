package com.tacz.guns.resource.manager;

import com.google.common.collect.Maps;
import com.google.gson.JsonParseException;
import com.tacz.guns.GunMod;
import com.tacz.guns.api.vmlib.LuaLibrary;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map.Entry;
import java.util.function.Supplier;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.resources.FileToIdConverter;
import org.jetbrains.annotations.NotNull;
import org.luaj.vm2.Globals;
import org.luaj.vm2.LoadState;
import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaFunction;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.compiler.LuaC;
import org.luaj.vm2.lib.Bit32Lib;
import org.luaj.vm2.lib.PackageLib;
import org.luaj.vm2.lib.TableLib;
import org.luaj.vm2.lib.jse.JseBaseLib;
import org.luaj.vm2.lib.jse.JseMathLib;
import org.luaj.vm2.lib.jse.JseStringLib;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

public class ScriptManager extends SimplePreparableReloadListener<List<Entry<String, Supplier<LuaTable>>>> implements IdentifiableResourceReloadListener {
   private static final Marker MARKER = MarkerFactory.getMarker("ScriptLoader");
   private Globals globals;
   private final Map<String, LuaTable> scriptMap = Maps.newHashMap();
   private final FileToIdConverter filetoidconverter;
   private final List<LuaLibrary> libraries;
   public static final Identifier ID = Identifier.fromNamespaceAndPath("tacz", "script_manager");

   public ScriptManager(FileToIdConverter converter, List<LuaLibrary> libraries) {
      this.filetoidconverter = converter;
      this.libraries = libraries;
   }

   @NotNull
   protected List<Entry<String, Supplier<LuaTable>>> prepare(ResourceManager pResourceManager, ProfilerFiller pProfiler) {
      this.initGlobals();
      List<Entry<String, Supplier<LuaTable>>> output = new ArrayList<>();

      for (Entry<Identifier, Resource> entry : this.filetoidconverter.listMatchingResources(pResourceManager).entrySet()) {
         final Entry<String, Supplier<LuaTable>> wrappedEntry = this.wrapLoadingFunction(entry.getKey(), entry.getValue());
         output.add(wrappedEntry);
         this.globals.get("package").get("preload").set(wrappedEntry.getKey(), new LuaFunction() {
            public LuaValue call(LuaValue modname, LuaValue env) {
               return (LuaValue)wrappedEntry.getValue().get();
            }
         });
      }

      return output;
   }

   protected void apply(List<Entry<String, Supplier<LuaTable>>> pObject, ResourceManager pResourceManager, ProfilerFiller pProfiler) {
      this.scriptMap.clear();
      pObject.forEach(entry -> this.scriptMap.put(entry.getKey(), entry.getValue().get()));
   }

   private Entry<String, Supplier<LuaTable>> wrapLoadingFunction(Identifier rawResourceLocation, Resource resource) {
      final Identifier resourceLocation = this.filetoidconverter.fileToId(rawResourceLocation);
      final String moduleName = this.getModuleName(resourceLocation);
      return new SimpleEntry<>(moduleName, new Supplier<LuaTable>() {
         private LuaTable loaded = null;

         public LuaTable get() {
            if (this.loaded != null) {
               return this.loaded;
            }

            try (Reader reader = resource.openAsReader()) {
               LuaValue chunk = ScriptManager.this.globals.load(reader, moduleName);
               this.loaded = chunk.call().checktable(1);
               return this.loaded;
            } catch (IllegalArgumentException | IOException | JsonParseException | LuaError jsonparseexception) {
               GunMod.LOGGER.warn(ScriptManager.MARKER, "Failed to read script file: {}", resourceLocation);
               return null;
            }
         }
      });
   }

   private void initGlobals() {
      this.globals = secureStandardGlobals();
      if (this.libraries != null) {
         this.libraries.forEach(library -> library.install(this.globals));
      }
   }

   private String getModuleName(Identifier resourceLocation) {
      return resourceLocation.getNamespace() + "_" + resourceLocation.getPath();
   }

   public LuaTable getScript(Identifier id) {
      return this.scriptMap.get(this.getModuleName(id));
   }

   private static Globals secureStandardGlobals() {
      Globals globals = new Globals();
      globals.load(new JseBaseLib());
      globals.load(new PackageLib());
      globals.load(new Bit32Lib());
      globals.load(new TableLib());
      globals.load(new JseStringLib());
      globals.load(new JseMathLib());
      LoadState.install(globals);
      LuaC.install(globals);
      return globals;
   }

   public Identifier getFabricId() {
      return ID;
   }
}
