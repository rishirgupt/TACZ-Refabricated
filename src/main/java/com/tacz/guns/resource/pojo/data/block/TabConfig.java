package com.tacz.guns.resource.pojo.data.block;

import cn.sh1rocu.tacz.TaCZFabric;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.serialization.JsonOps;
import com.tacz.guns.api.DefaultAssets;
import com.tacz.guns.api.item.builder.AmmoItemBuilder;
import com.tacz.guns.api.item.builder.AttachmentItemBuilder;
import com.tacz.guns.api.item.builder.GunItemBuilder;
import com.tacz.guns.init.ModItems;
import java.lang.reflect.Type;
import java.util.List;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.GsonHelper;
import org.jetbrains.annotations.NotNull;

public record TabConfig(Identifier id, String name, java.util.function.Supplier<ItemStack> iconSupplier) {
   public TabConfig(Identifier id, String name, ItemStack icon) {
      this(id, name, () -> icon);
   }

   /** Icons are resolved on first use: item components do not exist yet while data packs are being parsed. */
   public ItemStack icon() {
      return this.iconSupplier.get();
   }

   public static final Identifier TAB_AMMO = Identifier.fromNamespaceAndPath("tacz", "ammo");
   public static final Identifier TAB_PISTOL = Identifier.fromNamespaceAndPath("tacz", "pistol");
   public static final Identifier TAB_SNIPER = Identifier.fromNamespaceAndPath("tacz", "sniper");
   public static final Identifier TAB_RIFLE = Identifier.fromNamespaceAndPath("tacz", "rifle");
   public static final Identifier TAB_SHOTGUN = Identifier.fromNamespaceAndPath("tacz", "shotgun");
   public static final Identifier TAB_SMG = Identifier.fromNamespaceAndPath("tacz", "smg");
   public static final Identifier TAB_RPG = Identifier.fromNamespaceAndPath("tacz", "rpg");
   public static final Identifier TAB_MG = Identifier.fromNamespaceAndPath("tacz", "mg");
   public static final Identifier TAB_SCOPE = Identifier.fromNamespaceAndPath("tacz", "scope");
   public static final Identifier TAB_MUZZLE = Identifier.fromNamespaceAndPath("tacz", "muzzle");
   public static final Identifier TAB_STOCK = Identifier.fromNamespaceAndPath("tacz", "stock");
   public static final Identifier TAB_GRIP = Identifier.fromNamespaceAndPath("tacz", "grip");
   public static final Identifier TAB_EXTENDED_MAG = Identifier.fromNamespaceAndPath("tacz", "extended_mag");
   public static final Identifier TAB_LASER = Identifier.fromNamespaceAndPath("tacz", "laser");
   public static final Identifier TAB_MISC = Identifier.fromNamespaceAndPath("tacz", "misc");
   public static final Identifier TAB_EMPTY = Identifier.fromNamespaceAndPath("tacz", "empty");
   private static List<TabConfig> cachedDefaultTabs;

   /** Built on first use: the icons are item stacks, which cannot be created before item components are bound. */
   public static synchronized List<TabConfig> defaultTabs() {
      if (cachedDefaultTabs == null) {
         cachedDefaultTabs = buildDefaultTabs();
      }

      return cachedDefaultTabs;
   }

   private static List<TabConfig> buildDefaultTabs() {
      return List.of(
      new TabConfig(TAB_AMMO, "tacz.type.ammo.name", AmmoItemBuilder.create().setId(DefaultAssets.DEFAULT_AMMO_ID).build()),
      new TabConfig(
         TAB_PISTOL, "tacz.type.pistol.name", GunItemBuilder.create().setId(Identifier.fromNamespaceAndPath("tacz", "glock_17")).forceBuild(TaCZFabric.VANILLA_ACCESS)
      ),
      new TabConfig(
         TAB_SNIPER, "tacz.type.sniper.name", GunItemBuilder.create().setId(Identifier.fromNamespaceAndPath("tacz", "ai_awp")).forceBuild(TaCZFabric.VANILLA_ACCESS)
      ),
      new TabConfig(
         TAB_RIFLE, "tacz.type.rifle.name", GunItemBuilder.create().setId(Identifier.fromNamespaceAndPath("tacz", "ak47")).forceBuild(TaCZFabric.VANILLA_ACCESS)
      ),
      new TabConfig(
         TAB_SHOTGUN,
         "tacz.type.shotgun.name",
         GunItemBuilder.create().setId(Identifier.fromNamespaceAndPath("tacz", "db_short")).forceBuild(TaCZFabric.VANILLA_ACCESS)
      ),
      new TabConfig(
         TAB_SMG, "tacz.type.smg.name", GunItemBuilder.create().setId(Identifier.fromNamespaceAndPath("tacz", "hk_mp5a5")).forceBuild(TaCZFabric.VANILLA_ACCESS)
      ),
      new TabConfig(TAB_RPG, "tacz.type.rpg.name", GunItemBuilder.create().setId(Identifier.fromNamespaceAndPath("tacz", "rpg7")).forceBuild(TaCZFabric.VANILLA_ACCESS)),
      new TabConfig(TAB_MG, "tacz.type.mg.name", GunItemBuilder.create().setId(Identifier.fromNamespaceAndPath("tacz", "m249")).forceBuild(TaCZFabric.VANILLA_ACCESS)),
      new TabConfig(TAB_SCOPE, "tacz.type.scope.name", AttachmentItemBuilder.create().setId(Identifier.fromNamespaceAndPath("tacz", "scope_acog_ta31")).build()),
      new TabConfig(
         TAB_MUZZLE, "tacz.type.muzzle.name", AttachmentItemBuilder.create().setId(Identifier.fromNamespaceAndPath("tacz", "muzzle_compensator_trident")).build()
      ),
      new TabConfig(TAB_STOCK, "tacz.type.stock.name", AttachmentItemBuilder.create().setId(Identifier.fromNamespaceAndPath("tacz", "stock_militech_b5")).build()),
      new TabConfig(TAB_GRIP, "tacz.type.grip.name", AttachmentItemBuilder.create().setId(Identifier.fromNamespaceAndPath("tacz", "grip_magpul_afg_2")).build()),
      new TabConfig(
         TAB_EXTENDED_MAG, "tacz.type.extended_mag.name", AttachmentItemBuilder.create().setId(Identifier.fromNamespaceAndPath("tacz", "extended_mag_3")).build()
      ),
      new TabConfig(TAB_LASER, "tacz.type.laser.name", AttachmentItemBuilder.create().setId(Identifier.fromNamespaceAndPath("tacz", "laser_compact")).build()),
      new TabConfig(TAB_MISC, "tacz.type.misc.name", ModItems.GUN_SMITH_TABLE.getDefaultInstance())
   );
   }

   @NotNull
   public Component getName() {
      return Component.translatable(this.name == null ? "tacz.type.unknown.name" : this.name);
   }

   public static class Deserializer implements JsonDeserializer<TabConfig> {
      public TabConfig deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
         if (!json.isJsonObject()) {
            throw new JsonParseException("TabConfig must be a JSON object");
         } else {
            JsonObject object = json.getAsJsonObject();
            if (object.has("id") && object.get("id").isJsonPrimitive()) {
               Identifier id = (Identifier)context.deserialize(object.get("id"), Identifier.class);
               com.google.gson.JsonObject iconJson = GsonHelper.getAsJsonObject(object, "icon");
               java.util.concurrent.atomic.AtomicReference<ItemStack> iconCache = new java.util.concurrent.atomic.AtomicReference<>();
               java.util.function.Supplier<ItemStack> icon = () -> {
                  ItemStack cached = iconCache.get();
                  if (cached == null) {
                     try {
                        cached = (ItemStack)ItemStack.CODEC.parse(JsonOps.INSTANCE, com.tacz.guns.util.LegacyItemJson.fix(iconJson)).getOrThrow();
                     } catch (IllegalStateException e) {
                        return ItemStack.EMPTY;
                     }

                     iconCache.set(cached);
                  }

                  return cached;
               };

               String name = GsonHelper.getAsString(object, "name", "tacz.type.unknown.name");
               return new TabConfig(id, name, icon);
            } else {
               throw new JsonParseException("TabConfig must have an id");
            }
         }
      }
   }
}
