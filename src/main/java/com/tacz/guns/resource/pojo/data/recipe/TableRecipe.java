package com.tacz.guns.resource.pojo.data.recipe;

import com.google.gson.annotations.SerializedName;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.tacz.guns.crafting.GunSmithTableIngredient;
import com.tacz.guns.crafting.result.GunSmithTableResult;
import java.util.List;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public class TableRecipe {
   public static final MapCodec<TableRecipe> CODEC = RecordCodecBuilder.mapCodec(
      instance -> instance.group(
            GunSmithTableIngredient.CODEC.listOf().fieldOf("materials").forGetter(TableRecipe::getMaterials),
            GunSmithTableResult.CODEC.fieldOf("result").forGetter(TableRecipe::getResult)
         )
         .apply(instance, TableRecipe::new)
   );
   public static final StreamCodec<RegistryFriendlyByteBuf, TableRecipe> STREAM_CODEC = StreamCodec.composite(
      GunSmithTableIngredient.STREAM_CODEC.apply(ByteBufCodecs.collection(NonNullList::createWithCapacity)),
      TableRecipe::getMaterials,
      GunSmithTableResult.STREAM_CODEC,
      TableRecipe::getResult,
      TableRecipe::new
   );
   @SerializedName("materials")
   private List<GunSmithTableIngredient> materials;
   @SerializedName("result")
   private GunSmithTableResult result;

   public TableRecipe() {
   }

   public TableRecipe(List<GunSmithTableIngredient> materials, GunSmithTableResult result) {
      this.materials = materials;
      this.result = result;
   }

   public List<GunSmithTableIngredient> getMaterials() {
      return this.materials;
   }

   public GunSmithTableResult getResult() {
      return this.result;
   }
}
