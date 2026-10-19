package com.tacz.guns.client.render26;

import com.mojang.serialization.MapCodec;
import com.tacz.guns.item.AmmoBoxItem;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.numeric.RangeSelectItemModelProperty;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemStack;

/** Numeric item model property {@code tacz:ammo_statue}: which ammo box model (level, open or closed) to show. */
public record AmmoStatueProperty() implements RangeSelectItemModelProperty {
   public static final Identifier ID = Identifier.fromNamespaceAndPath("tacz", "ammo_statue");
   public static final MapCodec<AmmoStatueProperty> CODEC = MapCodec.unit(new AmmoStatueProperty());

   public static void register() {
      net.minecraft.client.renderer.item.properties.numeric.RangeSelectItemModelProperties.ID_MAPPER.put(ID, CODEC);
   }

   @Override
   public float get(ItemStack stack, ClientLevel level, ItemOwner owner, int seed) {
      return AmmoBoxItem.getStatue(stack, level, owner == null ? null : owner.asLivingEntity(), seed);
   }

   @Override
   public MapCodec<AmmoStatueProperty> type() {
      return CODEC;
   }
}
