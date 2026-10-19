package cn.sh1rocu.tacz.mixin.common;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.tacz.guns.init.ModRecipe;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RecipeManager.class)
public abstract class RecipeManagerMixin {
   /** Gun smith table recipes spell their custom ingredient type as "type"; Fabric expects "fabric:type". */
   @Inject(method = "fromJson", at = @At("HEAD"))
   private static void tacz$injectFromJson(ResourceKey<Recipe<?>> key, JsonObject obj, HolderLookup.Provider registries, CallbackInfoReturnable<?> cir) {
      if (obj.has("materials") && obj.has("type") && obj.get("type").getAsString().equals(ModRecipe.GUN_SMITH_TABLE_CRAFTING.toString())) {
         JsonArray materials = obj.getAsJsonArray("materials");
         materials.forEach(material -> {
            if (material.isJsonObject()) {
               JsonElement item = material.getAsJsonObject().get("item");
               if (item != null && item.isJsonObject() && item.getAsJsonObject().has("type")) {
                  JsonObject itemObj = item.getAsJsonObject();
                  itemObj.addProperty("fabric:type", itemObj.get("type").getAsString());
               }
            }
         });
      }
   }
}
