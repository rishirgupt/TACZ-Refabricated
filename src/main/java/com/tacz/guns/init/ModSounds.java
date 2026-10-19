package com.tacz.guns.init;

import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.core.registries.BuiltInRegistries;

public class ModSounds {
   public static final SoundEvent GUN = register("gun", SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath("tacz", "gun")));
   public static final SoundEvent TARGET_HIT = register("target_block_hit", SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath("tacz", "target_block_hit")));

   public static void init() {
   }

   private static SoundEvent register(String name, SoundEvent soundEvent) {
      return (SoundEvent)Registry.register(BuiltInRegistries.SOUND_EVENT, Identifier.fromNamespaceAndPath("tacz", name), soundEvent);
   }
}
