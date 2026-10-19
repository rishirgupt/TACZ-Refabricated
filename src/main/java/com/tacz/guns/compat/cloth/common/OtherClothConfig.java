package com.tacz.guns.compat.cloth.common;

import com.tacz.guns.config.PreLoadConfig;
import com.tacz.guns.config.common.OtherConfig;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.network.chat.Component;

public class OtherClothConfig {
   public static void init(ConfigBuilder root, ConfigEntryBuilder entryBuilder) {
      ConfigCategory other = root.getOrCreateCategory(Component.translatable("config.tacz.common.other"));
      other.addEntry(
         entryBuilder.startBooleanToggle(Component.translatable("config.tacz.common.other.default_pack_debug"), (Boolean)PreLoadConfig.override.get())
            .setDefaultValue(false)
            .setTooltip(new Component[]{Component.translatable("config.tacz.common.other.default_pack_debug.desc")})
            .setSaveConsumer(value -> {
               PreLoadConfig.override.set(value);
               PreLoadConfig.override.save();
            })
            .build()
      );
      other.addEntry(
         entryBuilder.startIntField(Component.translatable("config.tacz.common.other.target_sound_distance"), (Integer)OtherConfig.TARGET_SOUND_DISTANCE.get())
            .setMin(0)
            .setMax(Integer.MAX_VALUE)
            .setDefaultValue(128)
            .setTooltip(new Component[]{Component.translatable("config.tacz.common.other.target_sound_distance.desc")})
            .setSaveConsumer(value -> {
               OtherConfig.TARGET_SOUND_DISTANCE.set(value);
               OtherConfig.TARGET_SOUND_DISTANCE.save();
            })
            .build()
      );
   }
}
