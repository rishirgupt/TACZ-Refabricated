package cn.sh1rocu.tacz.util;

import java.util.List;
import java.util.Set;
import net.fabricmc.loader.api.FabricLoader;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

public class MixinPlugin implements IMixinConfigPlugin {
   public void onLoad(String s) {
   }

   public String getRefMapperConfig() {
      return null;
   }

   public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
      return mixinClassName.startsWith("cn.sh1rocu.tacz.mixin.compat.") ? FabricLoader.getInstance().isModLoaded(mixinClassName.split("\\.")[5]) : true;
   }

   public void acceptTargets(Set<String> set, Set<String> set1) {
   }

   public List<String> getMixins() {
      return null;
   }

   public void preApply(String s, ClassNode classNode, String s1, IMixinInfo iMixinInfo) {
   }

   public void postApply(String s, ClassNode classNode, String s1, IMixinInfo iMixinInfo) {
   }
}
