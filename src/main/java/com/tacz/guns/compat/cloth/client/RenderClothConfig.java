package com.tacz.guns.compat.cloth.client;

import com.tacz.guns.client.renderer.crosshair.CrosshairType;
import com.tacz.guns.compat.cloth.widget.CrosshairDropdown;
import com.tacz.guns.config.client.RenderConfig;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.stream.Collectors;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.network.chat.Component;

public class RenderClothConfig {
   public static void init(ConfigBuilder root, ConfigEntryBuilder entryBuilder) {
      ConfigCategory render = root.getOrCreateCategory(Component.translatable("config.tacz.client.render"));
      render.addEntry(
         entryBuilder.startBooleanToggle(Component.translatable("config.tacz.client.render.laser_fadeout"), (Boolean)RenderConfig.ENABLE_LASER_FADE_OUT.get())
            .setDefaultValue(true)
            .setTooltip(new Component[]{Component.translatable("config.tacz.client.render.laser_fadeout.desc")})
            .setSaveConsumer(value -> {
               RenderConfig.ENABLE_LASER_FADE_OUT.set(value);
               RenderConfig.ENABLE_LASER_FADE_OUT.save();
            })
            .build()
      );
      render.addEntry(
         entryBuilder.startIntField(
               Component.translatable("config.tacz.client.render.gun_lod_render_distance"), (Integer)RenderConfig.GUN_LOD_RENDER_DISTANCE.get()
            )
            .setMin(0)
            .setMax(Integer.MAX_VALUE)
            .setDefaultValue(0)
            .setTooltip(new Component[]{Component.translatable("config.tacz.client.render.gun_lod_render_distance.desc")})
            .setSaveConsumer(value -> {
               RenderConfig.GUN_LOD_RENDER_DISTANCE.set(value);
               RenderConfig.GUN_LOD_RENDER_DISTANCE.save();
            })
            .build()
      );
      render.addEntry(
         entryBuilder.startIntField(
               Component.translatable("config.tacz.client.render.bullet_hole_particle_life"), (Integer)RenderConfig.BULLET_HOLE_PARTICLE_LIFE.get()
            )
            .setMin(0)
            .setMax(Integer.MAX_VALUE)
            .setDefaultValue(400)
            .setTooltip(new Component[]{Component.translatable("config.tacz.client.render.bullet_hole_particle_life.desc")})
            .setSaveConsumer(value -> {
               RenderConfig.BULLET_HOLE_PARTICLE_LIFE.set(value);
               RenderConfig.BULLET_HOLE_PARTICLE_LIFE.save();
            })
            .build()
      );
      render.addEntry(
         entryBuilder.startDoubleField(
               Component.translatable("config.tacz.client.render.bullet_hole_particle_fade_threshold"),
               (Double)RenderConfig.BULLET_HOLE_PARTICLE_FADE_THRESHOLD.get()
            )
            .setMin(0.0)
            .setMax(1.0)
            .setDefaultValue(0.98)
            .setTooltip(new Component[]{Component.translatable("config.tacz.client.render.bullet_hole_particle_fade_threshold.desc")})
            .setSaveConsumer(value -> {
               RenderConfig.BULLET_HOLE_PARTICLE_FADE_THRESHOLD.set(value);
               RenderConfig.BULLET_HOLE_PARTICLE_FADE_THRESHOLD.save();
            })
            .build()
      );
      render.addEntry(
         entryBuilder.startDropdownMenu(
               Component.translatable("config.tacz.client.render.crosshair_type"),
               CrosshairDropdown.of((CrosshairType)RenderConfig.CROSSHAIR_TYPE.get()),
               CrosshairDropdown.of()
            )
            .setSelections(
               Arrays.stream(CrosshairType.values()).sorted().sorted(Comparator.comparing(Enum::name)).collect(Collectors.toCollection(LinkedHashSet::new))
            )
            .setDefaultValue(CrosshairType.DOT_1)
            .setTooltip(new Component[]{Component.translatable("config.tacz.client.render.crosshair_type.desc")})
            .setSaveConsumer(value -> {
               RenderConfig.CROSSHAIR_TYPE.set(value);
               RenderConfig.CROSSHAIR_TYPE.save();
            })
            .build()
      );
      render.addEntry(
         entryBuilder.startDoubleField(
               Component.translatable("config.tacz.client.render.hit_market_start_position"), (Double)RenderConfig.HIT_MARKET_START_POSITION.get()
            )
            .setMin(-1024.0)
            .setMax(1024.0)
            .setDefaultValue(4.0)
            .setTooltip(new Component[]{Component.translatable("config.tacz.client.render.hit_market_start_position.desc")})
            .setSaveConsumer(value -> {
               RenderConfig.HIT_MARKET_START_POSITION.set(value);
               RenderConfig.HIT_MARKET_START_POSITION.save();
            })
            .build()
      );
      render.addEntry(
         entryBuilder.startBooleanToggle(
               Component.translatable("config.tacz.client.render.head_shot_debug_hitbox"), (Boolean)RenderConfig.HEAD_SHOT_DEBUG_HITBOX.get()
            )
            .setDefaultValue(false)
            .setTooltip(new Component[]{Component.translatable("config.tacz.client.render.head_shot_debug_hitbox.desc")})
            .setSaveConsumer(value -> {
               RenderConfig.HEAD_SHOT_DEBUG_HITBOX.set(value);
               RenderConfig.HEAD_SHOT_DEBUG_HITBOX.save();
            })
            .build()
      );
      render.addEntry(
         entryBuilder.startBooleanToggle(Component.translatable("config.tacz.client.render.gun_hud_enable"), (Boolean)RenderConfig.GUN_HUD_ENABLE.get())
            .setDefaultValue(true)
            .setTooltip(new Component[]{Component.translatable("config.tacz.client.render.gun_hud_enable.desc")})
            .setSaveConsumer(value -> {
               RenderConfig.GUN_HUD_ENABLE.set(value);
               RenderConfig.GUN_HUD_ENABLE.save();
            })
            .build()
      );
      render.addEntry(
         entryBuilder.startBooleanToggle(
               Component.translatable("config.tacz.client.render.kill_amount_enable"), (Boolean)RenderConfig.KILL_AMOUNT_ENABLE.get()
            )
            .setDefaultValue(true)
            .setTooltip(new Component[]{Component.translatable("config.tacz.client.render.kill_amount_enable.desc")})
            .setSaveConsumer(value -> {
               RenderConfig.KILL_AMOUNT_ENABLE.set(value);
               RenderConfig.KILL_AMOUNT_ENABLE.save();
            })
            .build()
      );
      render.addEntry(
         entryBuilder.startDoubleField(
               Component.translatable("config.tacz.client.render.kill_amount_duration_second"), (Double)RenderConfig.KILL_AMOUNT_DURATION_SECOND.get()
            )
            .setMin(0.0)
            .setMax(Double.MAX_VALUE)
            .setDefaultValue(3.0)
            .setTooltip(new Component[]{Component.translatable("config.tacz.client.render.kill_amount_duration_second.desc")})
            .setSaveConsumer(value -> {
               RenderConfig.KILL_AMOUNT_DURATION_SECOND.set(value);
               RenderConfig.KILL_AMOUNT_DURATION_SECOND.save();
            })
            .build()
      );
      render.addEntry(
         entryBuilder.startIntField(
               Component.translatable("config.tacz.client.render.target_render_distance"), (Integer)RenderConfig.TARGET_RENDER_DISTANCE.get()
            )
            .setMin(0)
            .setMax(Integer.MAX_VALUE)
            .setDefaultValue(128)
            .setTooltip(new Component[]{Component.translatable("config.tacz.client.render.target_render_distance.desc")})
            .setSaveConsumer(value -> {
               RenderConfig.TARGET_RENDER_DISTANCE.set(value);
               RenderConfig.TARGET_RENDER_DISTANCE.save();
            })
            .build()
      );
      render.addEntry(
         entryBuilder.startBooleanToggle(
               Component.translatable("config.tacz.client.render.first_person_bullet_tracer_enable"),
               (Boolean)RenderConfig.FIRST_PERSON_BULLET_TRACER_ENABLE.get()
            )
            .setDefaultValue(true)
            .setTooltip(new Component[]{Component.translatable("config.tacz.client.render.first_person_bullet_tracer_enable.desc")})
            .setSaveConsumer(value -> {
               RenderConfig.FIRST_PERSON_BULLET_TRACER_ENABLE.set(value);
               RenderConfig.FIRST_PERSON_BULLET_TRACER_ENABLE.save();
            })
            .build()
      );
      render.addEntry(
         entryBuilder.startBooleanToggle(
               Component.translatable("config.tacz.client.render.disable_interact_hud_text"), (Boolean)RenderConfig.DISABLE_INTERACT_HUD_TEXT.get()
            )
            .setDefaultValue(false)
            .setTooltip(new Component[]{Component.translatable("config.tacz.client.render.disable_interact_hud_text.desc")})
            .setSaveConsumer(value -> {
               RenderConfig.DISABLE_INTERACT_HUD_TEXT.set(value);
               RenderConfig.DISABLE_INTERACT_HUD_TEXT.save();
            })
            .build()
      );
      render.addEntry(
         entryBuilder.startBooleanToggle(
               Component.translatable("config.tacz.client.render.auto_select_gun_smith_table_filter"),
               (Boolean)RenderConfig.AUTO_SELECT_GUN_SMITH_TABLE_FILTER.get()
            )
            .setDefaultValue(true)
            .setTooltip(new Component[]{Component.translatable("config.tacz.client.render.auto_select_gun_smith_table_filter.desc")})
            .setSaveConsumer(RenderConfig.AUTO_SELECT_GUN_SMITH_TABLE_FILTER::set)
            .build()
      );
      render.addEntry(
         entryBuilder.startIntField(
               Component.translatable("config.tacz.client.render.damage_counter_reset_time"), (Integer)RenderConfig.DAMAGE_COUNTER_RESET_TIME.get()
            )
            .setMin(10)
            .setMax(Integer.MAX_VALUE)
            .setDefaultValue(2000)
            .setTooltip(new Component[]{Component.translatable("config.tacz.client.render.damage_counter_reset_time.desc")})
            .setSaveConsumer(value -> {
               RenderConfig.DAMAGE_COUNTER_RESET_TIME.set(value);
               RenderConfig.DAMAGE_COUNTER_RESET_TIME.save();
            })
            .build()
      );
      render.addEntry(
         entryBuilder.startBooleanToggle(
               Component.translatable("config.tacz.client.render.disable_movement_fov"), (Boolean)RenderConfig.DISABLE_MOVEMENT_ATTRIBUTE_FOV.get()
            )
            .setDefaultValue(true)
            .setTooltip(new Component[]{Component.translatable("config.tacz.client.render.disable_movement_fov.desc")})
            .setSaveConsumer(value -> {
               RenderConfig.DISABLE_MOVEMENT_ATTRIBUTE_FOV.set(value);
               RenderConfig.DISABLE_MOVEMENT_ATTRIBUTE_FOV.save();
            })
            .build()
      );
      render.addEntry(
         entryBuilder.startBooleanToggle(
               Component.translatable("config.tacz.client.render.enable_tooltip_id"), (Boolean)RenderConfig.ENABLE_TACZ_ID_IN_TOOLTIP.get()
            )
            .setDefaultValue(true)
            .setTooltip(new Component[]{Component.translatable("config.tacz.client.render.enable_tooltip_id.desc")})
            .setSaveConsumer(value -> {
               RenderConfig.ENABLE_TACZ_ID_IN_TOOLTIP.set(value);
               RenderConfig.ENABLE_TACZ_ID_IN_TOOLTIP.save();
            })
            .build()
      );
      render.addEntry(
         entryBuilder.startBooleanToggle(
               Component.translatable("config.tacz.client.render.enable_translucent"), (Boolean)RenderConfig.BLOCK_ENTITY_TRANSLUCENT.get()
            )
            .setDefaultValue(false)
            .setTooltip(new Component[]{Component.translatable("config.tacz.client.render.enable_translucent.desc")})
            .setSaveConsumer(value -> {
               RenderConfig.BLOCK_ENTITY_TRANSLUCENT.set(value);
               RenderConfig.BLOCK_ENTITY_TRANSLUCENT.save();
            })
            .build()
      );
   }
}
