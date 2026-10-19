package com.tacz.guns.init;

import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.core.registries.BuiltInRegistries;

public class ModAttributes {
   public static final Attribute BULLET_RESISTANCE = register(
      "tacz.bullet_resistance", new RangedAttribute("attribute.name.tacz.bullet_resistance", 0.0, 0.0, 1.0).setSyncable(true)
   );

   public static void init() {
   }

   private static Attribute register(String name, Attribute attribute) {
      return (Attribute)Registry.register(BuiltInRegistries.ATTRIBUTE, Identifier.fromNamespaceAndPath("tacz", name), attribute);
   }
}
