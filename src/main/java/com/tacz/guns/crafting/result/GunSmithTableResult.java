package com.tacz.guns.crafting.result;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.tacz.guns.resource.pojo.data.block.TabConfig;
import com.tacz.guns.resource.pojo.data.recipe.GunResult;
import java.util.Optional;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.HolderLookup.Provider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GunSmithTableResult {
   public static final Codec<GunSmithTableResult> CODEC = ExtraCodecs.catchDecoderException(Codec.of(GunSmithTableResult::encode, GunSmithTableResult::decode));
   public static final StreamCodec<RegistryFriendlyByteBuf, GunSmithTableResult> STREAM_CODEC = StreamCodec.composite(
      ItemStack.OPTIONAL_STREAM_CODEC, GunSmithTableResult::getResult, Identifier.STREAM_CODEC, GunSmithTableResult::getGroup, GunSmithTableResult::new
   );
   public static final String GUN = "gun";
   public static final String AMMO = "ammo";
   public static final String ATTACHMENT = "attachment";
   public static final String CUSTOM = "custom";
   private ItemStack result = ItemStack.EMPTY;
   private Identifier group = null;
   @Nullable
   private RawGunTableResult raw = null;

   public static <T> DataResult<T> encode(GunSmithTableResult input, DynamicOps<T> ops, T prefix) {
      throw new UnsupportedOperationException();
   }

   public static <T> DataResult<Pair<GunSmithTableResult, T>> decode(DynamicOps<T> ops, T input) {
      return ops.getMap(input)
         .map(
            map -> {
               String typeName = (String)Codec.STRING.fieldOf("type").decode(ops, map).getOrThrow();
               int count = (Integer)Codec.INT.optionalFieldOf("count", 1).decode(ops, map).getOrThrow();
               CompoundTag extraTag = (CompoundTag)CompoundTag.CODEC.optionalFieldOf("nbt", null).decode(ops, map).getOrThrow();
               Identifier tabOverride = ((Optional<String>)Codec.STRING.optionalFieldOf("group").decode(ops, map).getOrThrow())
                  .map(rawx -> (String)rawx)
                  .map((String rawx) -> Identifier.tryParse(rawx.contains(":") ? rawx : "tacz:" + rawx))
                  .orElse(null);
               GunSmithTableResult result;
               switch (typeName) {
                  case "gun":
                  case "ammo":
                  case "attachment":
                     Identifier id = (Identifier)Identifier.CODEC.fieldOf("id").decode(ops, map).getOrThrow();
                     RawGunTableResult raw = new RawGunTableResult(typeName, id, count);
                     if (extraTag != null) {
                        raw.setNbt(extraTag);
                     }

                     if (typeName.equals("gun")) {
                        GunResult.CODEC.parse(ops, input).result().ifPresent(raw::setExtraData);
                     }

                     result = new GunSmithTableResult(raw, tabOverride);
                     break;
                  case "custom":
                     final CompoundTag finalExtraTag = extraTag;
                     // Item stacks cannot be decoded until item components are bound, so this waits for first use.
                     java.util.function.Supplier<ItemStack> lazyStack = () -> {
                        ItemStack itemStack = (ItemStack)ItemStack.OPTIONAL_CODEC.fieldOf("item").decode(ops, map).getOrThrow();
                        if (finalExtraTag != null) {
                           itemStack.update(DataComponents.CUSTOM_DATA, CustomData.EMPTY, data -> data.update(tag -> {
                              for (String key : finalExtraTag.keySet()) {
                                 Tag value = finalExtraTag.get(key);
                                 if (value != null) {
                                    tag.put(key, value);
                                 }
                              }
                           }));
                        }

                        return itemStack;
                     };
                     result = new GunSmithTableResult(lazyStack, tabOverride);
                     break;
                  default:
                     result = new GunSmithTableResult(ItemStack.EMPTY, TabConfig.TAB_EMPTY);
               }

               return Pair.of(result, input);
            }
         );
   }

   private java.util.function.Supplier<ItemStack> lazyResult;

   public GunSmithTableResult(java.util.function.Supplier<ItemStack> lazyResult, @Nullable Identifier group) {
      this.result = ItemStack.EMPTY;
      this.lazyResult = lazyResult;
      this.group = group == null ? TabConfig.TAB_EMPTY : group;
   }

   public GunSmithTableResult(ItemStack result, @Nullable Identifier group) {
      this.result = result;
      this.group = group == null ? TabConfig.TAB_EMPTY : group;
   }

   public GunSmithTableResult(@NotNull RawGunTableResult raw) {
      this.raw = raw;
   }

   public GunSmithTableResult(@NotNull RawGunTableResult raw, @Nullable Identifier group) {
      this.raw = raw;
      this.group = group == null ? TabConfig.TAB_EMPTY : group;
   }

   public void init(Provider provider) {
      this.resolveLazy();
      if (this.raw != null) {
         GunSmithTableResult result = RawGunTableResult.init(provider, this.raw);
         this.result = result.getResult();
         if (this.group == null || this.group.equals(TabConfig.TAB_EMPTY)) {
            this.group = result.getGroup();
         }

         this.raw = null;
      }
   }

   private void resolveLazy() {
      if (this.lazyResult != null) {
         this.result = this.lazyResult.get();
         this.lazyResult = null;
      }
   }

   public ItemStack getResult() {
      this.resolveLazy();
      return this.result;
   }

   public Identifier getGroup() {
      return this.group;
   }
}
