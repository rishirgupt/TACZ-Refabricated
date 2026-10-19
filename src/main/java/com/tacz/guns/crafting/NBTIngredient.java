package com.tacz.guns.crafting;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nullable;
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredient;
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredientSerializer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.core.Holder;
import java.util.stream.Stream;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.Identifier;
import net.minecraft.core.HolderSet;
import net.minecraft.resources.HolderSetCodec;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;

public class NBTIngredient implements CustomIngredient {
   public static final Codec<HolderSet<Item>> ITEM_HOLDER_SET_CODEC = HolderSetCodec.create(
      Registries.ITEM, BuiltInRegistries.ITEM.holderByNameCodec(), false
   );
   public static final MapCodec<NBTIngredient> CODEC = RecordCodecBuilder.mapCodec(
      builder -> builder.group(
            ITEM_HOLDER_SET_CODEC.fieldOf("items").forGetter(NBTIngredient::itemSet),
            TagParser.LENIENT_CODEC.fieldOf("nbt").forGetter(NBTIngredient::nbt),
            Codec.BOOL.optionalFieldOf("partial", true).forGetter(NBTIngredient::partial)
         )
         .apply(builder, NBTIngredient::new)
   );
   public static final StreamCodec<RegistryFriendlyByteBuf, NBTIngredient> STREAM_CODEC = StreamCodec.composite(
      ByteBufCodecs.fromCodecWithRegistries(ITEM_HOLDER_SET_CODEC),
      NBTIngredient::itemSet,
      ByteBufCodecs.COMPOUND_TAG,
      NBTIngredient::nbt,
      ByteBufCodecs.BOOL,
      NBTIngredient::partial,
      NBTIngredient::new
   );
   private final HolderSet<Item> items;
   private final CompoundTag nbt;
   private final boolean partial;
   private final ItemStack[] stacks;
   public static final Identifier ID = Identifier.fromNamespaceAndPath("tacz", "nbt");

   public NBTIngredient(HolderSet<Item> items, CompoundTag nbt, boolean partial) {
      this.items = items;
      this.nbt = nbt;
      this.partial = partial;
      DataComponentPatch patch = DataComponentPatch.builder().set(DataComponents.CUSTOM_DATA, CustomData.of(nbt)).build();
      this.stacks = items.stream().map(i -> new ItemStack(i, 1, patch)).toArray(ItemStack[]::new);
   }

   public boolean test(@Nullable ItemStack itemStack) {
      if (itemStack == null) {
         return false;
      }

      if (!this.items.contains(itemStack.typeHolder())) {
         return false;
      }

      CustomData data = (CustomData)itemStack.get(DataComponents.CUSTOM_DATA);
      CompoundTag nbt = data != null ? data.copyTag() : null;
      return this.partial ? (nbt != null && NbtUtils.compareNbt(this.nbt, nbt, true)) : Objects.equals(nbt, this.nbt);
   }

   public List<ItemStack> getMatchingStacks() {
      return Arrays.stream(this.stacks).toList();
   }

   public boolean requiresTesting() {
      return true;
   }

   public CustomIngredientSerializer<?> getSerializer() {
      return NBTIngredient.Serializer.INSTANCE;
   }

   @Override
   public Stream<Holder<Item>> items() {
      return this.items.stream();
   }

   public HolderSet<Item> itemSet() {
      return this.items;
   }

   public CompoundTag nbt() {
      return this.nbt;
   }

   public boolean partial() {
      return this.partial;
   }

   public static class Serializer implements CustomIngredientSerializer<NBTIngredient> {
      public static final NBTIngredient.Serializer INSTANCE = new NBTIngredient.Serializer();

      public Identifier getIdentifier() {
         return NBTIngredient.ID;
      }

      public MapCodec<NBTIngredient> getCodec() {
         return NBTIngredient.CODEC;
      }

      public StreamCodec<RegistryFriendlyByteBuf, NBTIngredient> getStreamCodec() {
         return NBTIngredient.STREAM_CODEC;
      }
   }
}
